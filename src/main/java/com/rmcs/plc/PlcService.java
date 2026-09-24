package com.rmcs.plc;

import com.github.s7connector.api.DaveArea;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.rmcs.model.SystemConfig;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
    /** S7 通讯默认端口（ISO-on-TCP / RFC1006），可通过参数设定覆盖 */
    private static final int DEFAULT_S7_PORT = 102;
    /** TCP 端口探测超时（毫秒），用于给出明确的连接失败原因 */
    private static final int TCP_PROBE_TIMEOUT_MS = 3000;

    private final String ip;
    private final int port;
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
    /** 实际握手成功的槽号（配置值握手失败时会自动切换 0↔1 再试） */
    private volatile int activeSlot;
    private volatile boolean connected;
    private volatile boolean polling;
    private int failCount;
    private int lastUpdateFlag = -1;
    private int mockFlag = 0;
    private int mockStep = 0;
    /** 是否已记录过「数据链路正常」，用于首次成功提示 */
    private boolean readOkLogged = false;
    /** 断线自动重连任务（成功或关闭时取消） */
    private java.util.concurrent.ScheduledFuture<?> reconnectTask;
    private int reconnectAttempts = 0;

    public PlcService(SystemConfig config, PlcListener listener) {
        this.ip = config.getPlcIp();
        int p = config.getPlcPort();
        this.port = (p <= 0 || p > 65535) ? DEFAULT_S7_PORT : p;
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
        this.activeSlot = this.slot;
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
        // ① 先探测配置的 IP:端口是否可达，无论成功失败都打印到日志
        listener.onStatus("网络端口探测：正在测试 " + ip + ":" + port
                + " 是否可达（超时 " + TCP_PROBE_TIMEOUT_MS + "ms）...");
        long t0 = System.currentTimeMillis();
        try (Socket probe = new Socket()) {
            probe.connect(new InetSocketAddress(ip, port), TCP_PROBE_TIMEOUT_MS);
            listener.onStatus("网络端口探测：✔ " + ip + ":" + port + " 可连通"
                    + "（耗时 " + (System.currentTimeMillis() - t0) + "ms），开始 S7 握手");
        } catch (Exception e) {
            String reason = tcpFailureReason(e);
            listener.onStatus("网络端口探测：✘ " + reason
                    + "（耗时 " + (System.currentTimeMillis() - t0) + "ms）");
            connected = false;
            listener.onConnectionFailed(reason);
            return;
        }

        // ② S7 握手（Rack/Slot 参与 COTP TSAP 计算）
        listener.onStatus("S7 握手：Rack=" + rack + ", Slot=" + slot + " ...");
        boolean usedAlt = false;
        try {
            open(rack, slot);
            activeSlot = slot;
        } catch (Exception first) {
            // S7-1200 不同固件用 0 或 1，自动切换另一个值再试一次
            int alt = slot == 0 ? 1 : 0;
            listener.onStatus("S7 握手：Slot=" + slot + " 失败（" + describe(first)
                    + "），自动尝试 Slot=" + alt + " ...");
            try {
                open(rack, alt);
                activeSlot = alt;
                usedAlt = true;
            } catch (Exception second) {
                connected = false;
                listener.onStatus("S7 握手：✘ 失败");
                listener.onConnectionFailed("S7 握手失败：" + describe(second) + " " + target()
                        + "（已尝试 Slot=" + slot + " 和 Slot=" + alt + "），请检查："
                        + "① TIA 中已勾选『允许来自远程对象的 PUT/GET 通信访问』"
                        + " ② DB 块已取消『优化的块访问』"
                        + " ③ 机架号是否为 0");
                return;
            }
        }
        connected = true;
        failCount = 0;
        listener.onStatus("S7 握手：✔ 成功（生效 Rack=" + rack + ", Slot=" + activeSlot + "）"
                + (usedAlt ? " — 已将槽号自动切换为 " + activeSlot + "，建议同步修改参数设定" : ""));
        listener.onConnected();
        startPolling();
    }

    /** 建立 S7 连接（COTP 握手，Rack/Slot 参与 TSAP 计算）。 */
    private void open(int rack, int slot) throws Exception {
        connector = S7ConnectorFactory.buildTCPConnector()
                .withHost(ip)
                .withPort(port)
                .withRack(rack)
                .withSlot(slot)
                .build();
    }

    /** 连接目标描述，例如 192.168.0.8:102(Rack=0,Slot=1)。 */
    private String target() {
        return "(目标 " + ip + ":" + port + " Rack=" + rack + " Slot=" + activeSlot + ")";
    }

    /** 把 TCP 探测异常翻译为可读的失败原因。 */
    private String tcpFailureReason(Exception e) {
        String t = "目标 " + ip + ":" + port;
        if (e instanceof UnknownHostException) {
            return "无法解析主机地址 " + ip + "，请检查 PLC IP 是否填写正确";
        }
        if (e instanceof SocketTimeoutException) {
            return "连接超时（" + t + " 在 " + TCP_PROBE_TIMEOUT_MS + "ms 内无响应）"
                    + "，请检查：① PLC 已上电 ② 网线/交换机 ③ 本机与 PLC 是否同一网段 ④ S7 端口默认 102";
        }
        if (e instanceof ConnectException) {
            String m = e.getMessage() == null ? "" : e.getMessage();
            if (m.contains("refused")) {
                return "连接被拒绝（" + t + "）：端口未开放，请确认 PLC 已启用 PUT/GET 通讯、端口 102 未被防火墙拦截";
            }
            return "无法建立 TCP 连接（" + t + "）：" + m;
        }
        if (e instanceof NoRouteToHostException) {
            return "网络不可达（" + t + "）：本机与 PLC 不在同一网段，请检查本机 IP/子网掩码";
        }
        return "TCP 连接失败（" + t + "）：" + describe(e);
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
            // read 参数顺序：(区域, DB号, 读取长度, 起始偏移) —— 长度在前、偏移在后
            byte[] data = connector.read(DaveArea.DB, dataDb, readLen, 0);
            failCount = 0;
            if (!readOkLogged) {
                readOkLogged = true;
                listener.onStatus("PLC 数据链路正常：已读取 DB" + dataDb + " 共 " + data.length
                        + " 字节，开始监听数据更新标志");
            }
            PlcData pd = parse(data);
            if (pd.getUpdateFlag() != lastUpdateFlag) {
                lastUpdateFlag = pd.getUpdateFlag();
                listener.onData(pd);
            }
        } catch (Exception e) {
            failCount++;
            // 首次异常立即打印，便于定位「连接成功但马上断线」
            if (failCount == 1) {
                listener.onStatus("PLC 读取异常：" + describe(e) + " " + target()
                        + "（读取 DB" + dataDb + " 长度 " + Math.max(updateFlagOff + 2, nameOff + 2 + productNameMaxLen)
                        + " 字节）");
            }
            if (failCount >= MAX_FAIL_BEFORE_OFFLINE) {
                connected = false;
                polling = false;
                listener.onDisconnected("连续 " + failCount + " 次读取失败：" + describe(e) + " " + target());
                startReconnect();
            }
        }
    }

    /** 断线后自动重连：每 5 秒尝试一次，成功后自动重新轮询。 */
    private void startReconnect() {
        if (mock || reconnectTask != null) return;
        reconnectTask = scheduler.scheduleWithFixedDelay(this::tryReconnect, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    private void tryReconnect() {
        if (connected) {
            cancelReconnect();
            return;
        }
        try {
            open(rack, activeSlot);
            connected = true;
            failCount = 0;
            readOkLogged = false;
            cancelReconnect();
            listener.onStatus("PLC 自动重连成功 " + target());
            listener.onConnected();
            startPolling();
        } catch (Exception e) {
            reconnectAttempts++;
            // 首次失败打印原因，后续静默重试，避免刷屏
            if (reconnectAttempts == 1) {
                listener.onStatus("PLC 自动重连失败：" + describe(e) + " " + target() + "（每 5 秒重试）");
            }
        }
    }

    private void cancelReconnect() {
        if (reconnectTask != null) {
            reconnectTask.cancel(false);
            reconnectTask = null;
            reconnectAttempts = 0;
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
    public int getPort() { return port; }
    public int getRack() { return rack; }
    public int getSlot() { return slot; }
    /** 实际握手成功使用的槽号（可能与配置值不同）。 */
    public int getActiveSlot() { return activeSlot; }
    public int getProductLen() { return productNameMaxLen; }
    public boolean isMock() { return mock; }
    public int getDataDb() { return dataDb; }
    public int getControlDb() { return controlDb; }

    /** 判断当前运行参数是否与最新配置一致；不一致则需要重启 PLC 服务。 */
    public boolean matchesConfig(SystemConfig cfg) {
        return cfg != null
                && cfg.getPlcIp().equals(ip)
                && cfg.getPlcPort() == port
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
        cancelReconnect();
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

    /** 异常详情：类型 + 消息 + 直接原因，便于定位。 */
    private String describe(Exception e) {
        StringBuilder sb = new StringBuilder(e.getClass().getSimpleName());
        String m = e.getMessage();
        if (m != null && !m.isBlank()) sb.append(": ").append(m);
        Throwable c = e.getCause();
        if (c != null) {
            sb.append(" | 原因: ").append(c.getClass().getSimpleName());
            String cm = c.getMessage();
            if (cm != null && !cm.isBlank()) sb.append(": ").append(cm);
        }
        return sb.toString();
    }
}
