package com.rmcs.plc;

import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.rmcs.model.SystemConfig;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * PLC（西门子 S7-1200/1500）通讯服务。
 *
 * <p>读取 DB36（PLC → 上位机映射），解析出当前作业的产品名 / 侧别 / 列 / 左·右 / 行，
 * 并在「数据更新标志」变化时通过 {@link PlcListener#onData} 回调，表示一个新的测点已就绪。
 *
 * <p>同时提供 {@link #writeWord} 用于把控制信号写回 DB37（上位机 → PLC 映射）。
 *
 * <p>离线调试时可使用 mock 模式：不连真实 PLC，由内部调度器模拟 Y1/Y2 两侧测点的轮转，
 * 便于在无硬件环境下验证界面与数据链路。
 */
public class PlcService {

    /** 连续读取失败多少次判定为断线 */
    private static final int MAX_FAIL_BEFORE_OFFLINE = 3;

    private final String ip;
    private final int rack;
    private final int slot;
    private final int productNameMaxLen;
    private final int pollMs;
    private final boolean mock;
    private final int dataDb;
    private final int heartbeatOff;
    private final int nameOff;
    private final int updateFlagOff;
    private final int sideOff;
    private final int rowOff;
    private final int colOff;
    private final int yCodeOff;
    private final int controlDb;
    private final int ackOff;
    private final int startOff;
    private final int stopOff;
    private final int resetOff;
    private final PlcListener listener;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "rmcs-plc");
                t.setDaemon(true);
                return t;
            });

    private S7Connector connector;
    private volatile boolean connected;
    private volatile boolean polling;
    private int failCount;
    private int lastUpdateFlag = -1;
    private int mockFlag = 0;
    private int mockStep = 0;

    public PlcService(SystemConfig config, PlcListener listener) {
        this.ip = config.getPlcIp();
        this.rack = config.getPlcRack();
        this.slot = config.getPlcSlot();
        this.productNameMaxLen = Math.max(8, config.getPlcProductLen());
        int pms = config.getPlcPollMs();
        this.pollMs = pms <= 0 ? 200 : pms;
        this.mock = config.isPlcMock();
        this.dataDb = config.getPlcDataDb();
        this.heartbeatOff = config.getPlcHeartbeatOffset();
        this.nameOff = config.getPlcNameOffset();
        this.updateFlagOff = config.getPlcUpdateFlagOffset();
        this.sideOff = config.getPlcSideOffset();
        this.rowOff = config.getPlcRowOffset();
        this.colOff = config.getPlcColOffset();
        this.yCodeOff = config.getPlcYCodeOffset();
        this.controlDb = config.getPlcControlDb();
        this.ackOff = config.getPlcAckOffset();
        this.startOff = config.getPlcStartOffset();
        this.stopOff = config.getPlcStopOffset();
        this.resetOff = config.getPlcResetOffset();
        this.listener = listener;
    }

    /** 建立连接并启动轮询（mock 模式直接启动模拟器）。 */
    public void connect() {
        if (mock) {
            connected = true;
            listener.onConnected();
            startMock();
            return;
        }
        try {
            scheduler.execute(this::connectInternal);
        } catch (Exception e) {
            listener.onConnectionFailed(describe(e));
        }
    }

    private void connectInternal() {
        try {
            connector = S7ConnectorFactory.buildTCPConnector()
                    .withHost(ip)
                    .withRack(rack)
                    .withSlot(slot)
                    .build();
            connected = true;
            failCount = 0;
            listener.onConnected();
            startPolling();
        } catch (Exception e) {
            connected = false;
            listener.onConnectionFailed(describe(e));
        }
    }

    private void startPolling() {
        if (!connected || polling) return;
        polling = true;
        scheduler.scheduleWithFixedDelay(this::pollOnce, pollMs, pollMs, TimeUnit.MILLISECONDS);
    }

    private void pollOnce() {
        if (!connected || connector == null) return;
        try {
            // 读取长度必须覆盖所有已配置偏移（每个 INT 占 2 字节）
            int maxOff = Math.max(updateFlagOff, Math.max(sideOff,
                    Math.max(rowOff, Math.max(colOff, yCodeOff))));
            int readLen = Math.max(maxOff + 2, nameOff + 2 + productNameMaxLen);
            byte[] data = connector.read(DaveArea.DB, dataDb, 0, readLen);
            failCount = 0;
            PlcData pd = parse(data);
            if (pd.getUpdateFlag() != lastUpdateFlag) {
                lastUpdateFlag = pd.getUpdateFlag();
                listener.onData(pd);
            }
        } catch (Exception e) {
            failCount++;
            if (failCount >= MAX_FAIL_BEFORE_OFFLINE) {
                connected = false;
                polling = false;
                listener.onDisconnected("连续 " + failCount + " 次读取失败：" + describe(e));
            }
        }
    }

    /** 解析数据区字节流为 {@link PlcData}，所有偏移从配置读取。 */
    private PlcData parse(byte[] b) {
        PlcData pd = new PlcData();
        pd.setHeartbeat(readInt(b, heartbeatOff));
        pd.setProductName(readS7String(b, nameOff));
        pd.setUpdateFlag(readInt(b, updateFlagOff));
        int lrCode = readInt(b, sideOff);
        pd.setLr(lrCode == 2 ? "右" : "左");
        pd.setRow(readInt(b, rowOff));
        pd.setCol(readInt(b, colOff));
        int yCode = readInt(b, yCodeOff);
        pd.setSide(yCode == 2 ? "Y2" : "Y1");
        return pd;
    }

    /** 大端 2 字节 INT。 */
    private static int readInt(byte[] b, int off) {
        if (b == null || off + 1 >= b.length) return 0;
        return ((b[off] & 0xFF) << 8) | (b[off + 1] & 0xFF);
    }

    /** 解析 S7 STRING：byte[off]=最大长度, byte[off+1]=实际长度, 之后为字符。 */
    private String readS7String(byte[] b, int off) {
        if (b == null || off + 2 >= b.length) return "";
        int act = b[off + 1] & 0xFF;
        int max = b[off] & 0xFF;
        if (act <= 0) return "";
        int start = off + 2;
        if (start + act > b.length) act = b.length - start;
        if (act <= 0) return "";
        // 兼容：若首字节不像合法 STRING 头（max 远小于 act），按纯字符数组处理
        if (max < act) max = act;
        return new String(b, start, act, StandardCharsets.ISO_8859_1).trim();
    }

    /**
     * 向 PLC 写入一个 16 位字（上位机 → PLC 控制信号）。
     * 例如回写「完成一次记录的标志位」DB37.DBW0 = 1。
     */
    public void writeWord(int db, int byteOffset, int value) {
        // 未连接 / 模拟模式下不执行任何 PLC 写入，避免静默「假成功」
        if (mock || !connected || connector == null) return;
        byte[] b = {(byte) (value >>> 8), (byte) value};
        try {
            connector.write(DaveArea.DB, db, byteOffset, b);
        } catch (Exception ignored) {
            // 控制信号写入失败不影响采集主链路，由调用方感知状态
        }
    }

    public boolean isConnected() { return connected; }

    public String getIp() { return ip; }
    public int getRack() { return rack; }
    public int getSlot() { return slot; }
    public int getProductLen() { return productNameMaxLen; }
    public boolean isMock() { return mock; }
    public int getDataDb() { return dataDb; }
    public int getControlDb() { return controlDb; }

    /** 判断当前运行参数是否与最新配置一致；不一致则需要重启 PLC 服务。 */
    public boolean matchesConfig(SystemConfig cfg) {
        return cfg != null
                && cfg.getPlcIp().equals(ip)
                && cfg.getPlcRack() == rack
                && cfg.getPlcSlot() == slot
                && cfg.getPlcProductLen() == productNameMaxLen
                && cfg.getPlcPollMs() == pollMs
                && cfg.isPlcMock() == mock
                && cfg.getPlcDataDb() == dataDb
                && cfg.getPlcHeartbeatOffset() == heartbeatOff
                && cfg.getPlcNameOffset() == nameOff
                && cfg.getPlcUpdateFlagOffset() == updateFlagOff
                && cfg.getPlcSideOffset() == sideOff
                && cfg.getPlcRowOffset() == rowOff
                && cfg.getPlcColOffset() == colOff
                && cfg.getPlcYCodeOffset() == yCodeOff
                && cfg.getPlcControlDb() == controlDb
                && cfg.getPlcAckOffset() == ackOff
                && cfg.getPlcStartOffset() == startOff
                && cfg.getPlcStopOffset() == stopOff
                && cfg.getPlcResetOffset() == resetOff;
    }

    public void shutdown() {
        polling = false;
        try {
            scheduler.submit(() -> {
                if (connector != null) {
                    try { connector.close(); } catch (Exception ignored) { }
                }
            });
        } catch (Exception ignored) { }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) scheduler.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connected = false;
    }

    /* ===== Mock 模拟器：无 PLC 时模拟测点轮转 ===== */
    private void startMock() {
        scheduler.scheduleWithFixedDelay(this::mockTick, 1200, 1200, TimeUnit.MILLISECONDS);
    }

    private void mockTick() {
        // 顺序：Y1/Y2 × 列1/2 × 左/右 × 行1..15，模拟生产线每个测点就绪
        int total = 60; // 每侧 60 个测点（2列×2左右×15行）
        int idx = mockStep % (total * 2);
        int sideIdx = idx / total;
        int within = idx % total;
        int row = within / 4 + 1;
        int rem = within % 4;
        int col = rem / 2 + 1;
        String lrStr = (rem % 2 == 0) ? "左" : "右";
        String sideStr = sideIdx == 0 ? "Y1" : "Y2";
        mockStep++;
        mockFlag = mockFlag >= 1000 ? 1 : mockFlag + 1;

        PlcData pd = new PlcData();
        pd.setHeartbeat(mockFlag);
        pd.setProductName(sideIdx == 0 ? "BGA-2835 左侧组件" : "BGA-2835 右侧组件");
        pd.setUpdateFlag(mockFlag);
        pd.setSide(sideStr);
        pd.setCol(col);
        pd.setLr(lrStr);
        pd.setRow(row);
        lastUpdateFlag = pd.getUpdateFlag();
        listener.onData(pd);
    }

    private String describe(Exception e) {
        String m = e.getMessage();
        return (m == null || m.isBlank()) ? e.getClass().getSimpleName() : m;
    }
}
