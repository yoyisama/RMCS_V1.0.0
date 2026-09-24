package com.rmcs.controller;

import com.rmcs.App;
import com.rmcs.device.HiokiResistanceMeter;
import com.rmcs.device.Measurement;
import com.rmcs.device.SerialConfig;
import com.rmcs.model.AuthUser;
import com.rmcs.model.InspectionRecord;
import com.rmcs.model.LogEntry;
import com.rmcs.model.LogEntry.LogType;
import com.rmcs.model.MatrixCell;
import com.rmcs.model.WorkPos;
import com.rmcs.model.ProductionStatus;
import com.rmcs.model.SystemConfig;
import com.rmcs.service.MeasurementListener;
import com.rmcs.service.MeterService;
import com.rmcs.plc.PlcData;
import com.rmcs.plc.PlcListener;
import com.rmcs.plc.PlcService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** 主控制台：聚合 Header/Footer/各面板 与 设备服务。 */
public class MainController implements MeasurementListener {

    /** 电阻计断线后的自动重连周期（秒）。 */
    private static final int RECONNECT_PERIOD_SECONDS = 15;
    /** 系统日志保留上限。 */
    private static final int MAX_LOGS = 200;

    /**
     * fx:include 子控制器注入：FXMLLoader 会把 fx:id="header" 的 include 文档的
     * controller 自动注入名为 headerController 的字段（fx:id + "Controller" 命名约定）。
     */
    /** 主控台主体区域：水平主容器 / 左侧工作区 / 右侧控制面板 */
    @FXML private HBox mainAnchor;
    @FXML private VBox workspace;
    @FXML private ScrollPane rightScroll;

    /** 工作区：Y1/Y2 两个 Tab，每 Tab 含单侧作业矩阵 + 单侧数据履历 + 系统运行日志 */
    @FXML private TabPane sideTabs;
    @FXML private WorkMatrixSideController workMatrixY1Controller, workMatrixY2Controller;
    @FXML private DataHistorySideController dataHistoryY1Controller, dataHistoryY2Controller;
    @FXML private ActionLogController actionLogY1Controller, actionLogY2Controller;

    @FXML private HeaderController headerController;
    @FXML private FooterBarController footerBarController;
    @FXML private RightPanelController rightPanelController;

    /** Y1/Y2 两个 Tab（PLC 读到哪一侧数据即自动切到对应 Tab）。 */
    @FXML private Tab tabY1, tabY2;

    /** Y1/Y2 全部履历总表（导出 CSV 与「履历查询」弹窗使用）。 */
    private final ObservableList<InspectionRecord> allRecords = FXCollections.observableArrayList();

    /** Y1/Y2 两个 Tab 共用的系统运行日志（同一份，避免分叉）。 */
    private final ObservableList<LogEntry> sharedLogs = FXCollections.observableArrayList();

    private App app;
    private AuthUser user;
    private SystemConfig cfg;
    private ProductionStatus status;
    private MeterService meterService;
    private PlcService plcService;
    private Timeline statusTick;
    /** 最近一次电阻计有效读数（由 PLC 侧触发采集时取用）。 */
    private Double lastMeterValue;
    /** 当前产品名称（来自 PLC DB36 产品名）。 */
    private String currentProductName = "";
    /** 断线重连倒计时（秒）。 */
    private int reconnectCountdown = RECONNECT_PERIOD_SECONDS;
    /** 当前主题：false=深色 root-dark，true=明亮 root-light（默认白天模式，模态窗口需跟随）。 */
    private boolean lightTheme = true;

    /** 由 App 在登录跳转后调用。 */
    public void initContext(App app, AuthUser user) {
        this.app = app;
        this.user = user;
        Platform.runLater(this::bootstrap);
    }

    /** 右面板占主体宽度的比例 */
    private static final double RIGHT_PANEL_RATIO = 0.26;
    /** 右面板最小宽度（需略大于 right_panel.fxml 内容 minWidth，留出滚动条空间） */
    private static final double RIGHT_PANEL_MIN = 300;

    /**
     * 确定性布局：按主体宽度实时计算右面板宽度，并【显式钉死工作区宽度】。
     * HBox 的自动收缩在个别环境下不生效（工作区按 pref 占满、右面板被推出窗口外），
     * 因此两侧宽度全部由代码显式设定，不依赖布局容器的自适应行为。
     */
    private void layoutRightPanel() {
        if (mainAnchor == null || rightScroll == null || workspace == null) return;
        double total = mainAnchor.getWidth();
        if (total <= 0) return;
        double w = Math.max(RIGHT_PANEL_MIN, Math.min(360, total * RIGHT_PANEL_RATIO));
        rightScroll.setMinWidth(w);
        rightScroll.setPrefWidth(w);
        rightScroll.setMaxWidth(w);
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // 高度强制等于视口：右侧四张卡片完整展示，与左侧视图同高
        rightScroll.setFitToHeight(true);
        // HBox: padding(8+4) + spacing(8) = 20 水平开销
        double ws = Math.max(120, total - 20 - w);
        workspace.setMinWidth(Math.min(380, ws));
        workspace.setPrefWidth(ws);
        workspace.setMaxWidth(ws);
        dbgLayout("total=" + Math.round(total) + " rightW=" + Math.round(w) + " ws=" + Math.round(ws));
    }

    /** 布局诊断：写入 target/theme_debug.txt（javaw 无控制台，println 不可见）。 */
    private void dbgLayout(String line) {
        try {
            java.nio.file.Files.write(java.nio.file.Paths.get("d:/work/yoyisama/RMCS_V1.0.0/target/theme_debug.txt"),
                    ("[LAYOUT] " + line + "\n").getBytes("UTF-8"),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }

    private void bootstrap() {
        cfg = new SystemConfig();
        status = new ProductionStatus();

        if (mainAnchor != null) {
            mainAnchor.widthProperty().addListener((o, a, b) -> layoutRightPanel());
            layoutRightPanel();
        }
        // 三大模块已改为页签布局（TabPane），每个模块独占全部工作区高度，无需比例分配
        // 场景尺寸监听 + 多帧重排：登录后场景/窗口尺寸是分帧才稳定的，
        // 只在 mainAnchor 上监听会漏掉「首帧尺寸已确定、后续无变化」的情况，
        // 导致右面板停留在旧宽度（表现为登录后截断、拖动窗口才正常）
        if (workspace != null) {
            Runnable bindScene = () -> {
                Scene s = workspace.getScene();
                if (s == null) return;
                s.widthProperty().addListener((o, a, b) -> layoutRightPanel());
                s.heightProperty().addListener((o, a, b) -> layoutRightPanel());
            };
            workspace.sceneProperty().addListener((o, a, b) -> {
                bindScene.run();
                Platform.runLater(() -> Platform.runLater(this::layoutRightPanel));
            });
            bindScene.run();
            Platform.runLater(() -> Platform.runLater(() -> {
                layoutRightPanel();
                Platform.runLater(this::layoutRightPanel);
            }));
        }

        if (headerController != null) {
            headerController.setContext(app, user);
            headerController.setOnLogout(this::logout);
            headerController.setOnModeChange(this::applyAutoMode);
            headerController.setOnThemeChange(() -> lightTheme = !lightTheme);
        }

        if (footerBarController != null) {
            footerBarController.setPlcOnline(true);
            footerBarController.setMeterOnline(false);
            footerBarController.setConfig(cfg.getComPort(), cfg.getPollIntervalMs());
            footerBarController.setBaudRate(cfg.getBaudRate());
            footerBarController.setOnReconnect(this::reconnectMeter);
        }

        if (rightPanelController != null) {
            rightPanelController.bind(cfg, status);
            rightPanelController.setOnStandardConfig(this::openStandardConfig);
            rightPanelController.setOnHistoryQuery(this::openHistoryQuery);
            rightPanelController.setOnLogQuery(this::openLogQuery);
            rightPanelController.setOnExport(this::exportCsv);
            rightPanelController.setOnStart(this::startMachine);
            rightPanelController.setOnStop(this::stopMachine);
            rightPanelController.setOnReset(this::resetMachine);
            rightPanelController.setOnCapture(this::captureOnce);
            rightPanelController.setOnIntervalChange(this::refreshMeterInterval);
            rightPanelController.setOnResetCounter(() -> {
                status.resetCounters();
                rightPanelController.refreshStatus();
                appendLog(LogType.INFO, "操作员清零完成数量与运行时长计数器");
            });
        }

        // Y1/Y2 两个 Tab 各自注入单侧作业矩阵、单侧数据履历，并共享同一份系统日志
        workMatrixY1Controller.setSide("Y1");
        workMatrixY2Controller.setSide("Y2");
        dataHistoryY1Controller.setSide("Y1");
        dataHistoryY2Controller.setSide("Y2");
        actionLogY1Controller.useList(sharedLogs);
        actionLogY2Controller.useList(sharedLogs);
        dataHistoryY1Controller.setOnOpen(this::openHistoryQuery);
        dataHistoryY2Controller.setOnOpen(this::openHistoryQuery);
        actionLogY1Controller.setOnOpen(this::openLogQuery);
        actionLogY2Controller.setOnOpen(this::openLogQuery);
        startPlcService();

        appendLog(LogType.INFO, "控制台已加载 · 操作员=" + (user == null ? "匿名" : user.getName()));

        statusTick = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (status.getStatus() == ProductionStatus.MachineStatus.RUNNING) {
                status.incrementDuration();
            }
            if (rightPanelController != null) rightPanelController.refreshStatus();
            tickReconnect();
        }));
        statusTick.setCycleCount(Timeline.INDEFINITE);
        statusTick.play();

        startMeterService();

        // 首帧布局自检：个别显卡/DPI 环境下首帧会把右侧面板排到场景右缘之外，
        // 检测到溢出时自动执行一次「等效拖动」的窗口尺寸重排（±1px），并记录诊断日志
        Platform.runLater(() -> Platform.runLater(() -> {
            try {
                layoutRightPanel();
                javafx.scene.Scene scene = rightPanelController == null ? null : rightPanelController.getScene();
                if (scene == null || scene.getWindow() == null) return;
                javafx.scene.Node right = scene.lookup("#rightScroll");
                if (right == null) return;
                javafx.geometry.Bounds b = right.localToScene(right.getBoundsInLocal());
                double overflow = b.getMaxX() - scene.getWidth();
                System.out.println("[RMCS-CHECK] sceneW=" + scene.getWidth()
                        + " rightMinX=" + Math.round(b.getMinX())
                        + " rightMaxX=" + Math.round(b.getMaxX())
                        + " overflow=" + Math.round(overflow));
                if (overflow > 1 || b.getMinX() < -1) {
                    javafx.stage.Stage st = (javafx.stage.Stage) scene.getWindow();
                    double sw = scene.getWidth();
                    double sh = scene.getHeight();
                    // 跨脉冲抖动才能触发真实 resize；同脉冲内 set/restore 会被 JavaFX 合并
                    javafx.application.Platform.runLater(() -> {
                        st.setWidth(sw - 1);
                        javafx.application.Platform.runLater(() -> {
                            st.setWidth(sw);
                            st.setHeight(sh - 1);
                            javafx.application.Platform.runLater(() -> st.setHeight(sh));
                        });
                    });
                    appendLog(LogType.WARNING, String.format(
                            "检测到首帧布局溢出 %.0fpx，已自动校正窗口布局（场景 %.0f×%.0f，右面板右缘 %.0f）",
                            Math.abs(overflow), sw, sh, b.getMaxX()));
                }
            } catch (Exception ignored) {
                // 自检失败不影响正常使用
            }
        }));
    }

    private void startMeterService() {
        SerialConfig sc = buildSerialConfig();
        HiokiResistanceMeter meter = new HiokiResistanceMeter(sc);
        meterService = new MeterService(meter, sc, this);
        appendLog(LogType.PLC, "PLC 总线通讯就绪，请求连接 " + sc.getPortName() + " @" + sc.getBaudRate() + "bps ...");
        meterService.connect();
    }

    /** 由当前 UI 配置构建串口参数，保证参数设定窗口的串口/波特率能真正下发到设备层。 */
    private SerialConfig buildSerialConfig() {
        SerialConfig sc = new SerialConfig();
        sc.setPortName(cfg.getComPort());
        sc.setBaudRate(cfg.getBaudRate());
        sc.setStandardValue(cfg.getStandard().getStandardValue());
        sc.setUpperDev(cfg.getStandard().getUpperDev());
        sc.setLowerDev(cfg.getStandard().getLowerDev());
        sc.setPollIntervalMs(currentIntervalMs());
        return sc;
    }

    private long currentIntervalMs() {
        return rightPanelController == null ? cfg.getPollIntervalMs() : rightPanelController.getIntervalMs();
    }

    /** 取指定侧别的作业矩阵控制器。 */
    private WorkMatrixSideController matrixOf(String side) {
        return "Y2".equals(side) ? workMatrixY2Controller : workMatrixY1Controller;
    }

    /** 取指定侧别的数据履历控制器。 */
    private DataHistorySideController historyOf(String side) {
        return "Y2".equals(side) ? dataHistoryY2Controller : dataHistoryY1Controller;
    }

    /** 两侧作业矩阵同步刷新在线状态徽标。 */
    private void setMatrixLive(ProductionStatus.MachineStatus s) {
        if (workMatrixY1Controller != null) workMatrixY1Controller.setLiveStatus(s);
        if (workMatrixY2Controller != null) workMatrixY2Controller.setLiveStatus(s);
    }

    /** 清空两侧作业矩阵。 */
    private void clearMatrix() {
        if (workMatrixY1Controller != null) workMatrixY1Controller.clearAll();
        if (workMatrixY2Controller != null) workMatrixY2Controller.clearAll();
    }

    /** 清空两侧数据履历。 */
    private void clearHistory() {
        if (dataHistoryY1Controller != null) dataHistoryY1Controller.clearAll();
        if (dataHistoryY2Controller != null) dataHistoryY2Controller.clearAll();
    }

    /** 启动 PLC 通讯服务（或离线模拟），并注册数据回调。 */
    private void startPlcService() {
        if (cfg == null) return;
        plcService = new PlcService(cfg, new PlcListener() {
                    @Override public void onConnected() {
                        Platform.runLater(() -> {
                            if (footerBarController != null) footerBarController.setPlcOnline(true);
                            appendLog(LogType.PLC, cfg.isPlcMock()
                                    ? "PLC 离线模拟模式已启动（不连接真实设备）"
                                    : "PLC(" + cfg.getPlcIp() + ") 连接成功");
                        });
                    }
                    @Override public void onConnectionFailed(String reason) {
                        Platform.runLater(() -> {
                            if (footerBarController != null) footerBarController.setPlcOnline(false);
                            appendLog(LogType.ERROR, "PLC 连接失败: " + reason);
                        });
                    }
                    @Override public void onDisconnected(String reason) {
                        Platform.runLater(() -> {
                            if (footerBarController != null) footerBarController.setPlcOnline(false);
                            appendLog(LogType.ERROR, "PLC 已断线: " + reason);
                        });
                    }
                    @Override public void onData(PlcData data) {
                        Platform.runLater(() -> onPlcData(data));
                    }
                });
        plcService.connect();
    }

    /** PLC 数据更新标志变化：自动切到对应 Tab，并以最近一次电阻计读数生成一条履历。 */
    private void onPlcData(PlcData data) {
        // 仅在 PLC 真正连接（含模拟）时处理；未连接则不走任何 PLC 相关业务逻辑
        if (data == null || sideTabs == null) return;
        if (plcService == null || !plcService.isConnected()) return;
        // 自动切换到该侧 Tab（PLC 读到哪一侧，界面就显示哪一侧）
        if ("Y2".equals(data.getSide()) && tabY2 != null) {
            sideTabs.getSelectionModel().select(tabY2);
        } else if (tabY1 != null) {
            sideTabs.getSelectionModel().select(tabY1);
        }

        currentProductName = data.getProductName();
        if (workMatrixY1Controller != null) workMatrixY1Controller.setProductName(currentProductName);
        if (workMatrixY2Controller != null) workMatrixY2Controller.setProductName(currentProductName);

        if (lastMeterValue == null) {
            appendLog(LogType.WARNING, "PLC 上报新测点（" + data.getSide() + " "
                    + data.getCol() + "列" + data.getLr() + WorkPos.rowName(data.getRow())
                    + "），但暂无电阻计读数，已跳过");
            return;
        }

        double value = lastMeterValue;
        double std = cfg.getStandard().getStandardValue();
        boolean pass = value >= cfg.getStandard().getLowerLimit()
                && value <= cfg.getStandard().getUpperLimit();

        WorkPos pos = new WorkPos(data.getSide(), data.getCol(), data.getLr(), data.getRow());
        WorkMatrixSideController mc = matrixOf(pos.getSide());
        if (mc != null) {
            mc.setCell(pos, new MatrixCell(value, pass, false));
            mc.setLiveStatus(status.getStatus());
        }

        InspectionRecord rec = new InspectionRecord(
                UUID.randomUUID().toString(),
                pos.getSide(), pos.getCol(), pos.getLr(), pos.getRow(), currentProductName,
                value, std, pass);
        historyOf(pos.getSide()).add(rec);
        allRecords.add(0, rec);
        if (allRecords.size() > 200) allRecords.remove(200, allRecords.size());

        status.incrementCompleted();
        if (rightPanelController != null) rightPanelController.refreshStatus();

        // 回写「完成一次记录的标志位」（PLC 自动清零）
        plcWrite(cfg.getPlcControlDb(), cfg.getPlcAckOffset(), 1);

        appendLog(pass ? LogType.SUCCESS : LogType.WARNING,
                String.format("[PLC采集] %s %s 阻值: %.3fΩ (%s)",
                        data.getSide(), pos.describe(), value, pass ? "合格" : "超差"));
    }

    /** 经 PLC 下发一个 16 位控制字；若 PLC 未连接则明确报错且不执行任何 PLC 逻辑。 */
    private void plcWrite(int db, int byteOffset, int value) {
        if (plcService == null) {
            appendLog(LogType.ERROR, "PLC 服务未初始化，控制信号（DB" + db + ".DBW" + byteOffset + "）未下发");
            return;
        }
        if (!plcService.isConnected()) {
            appendLog(LogType.ERROR, "PLC 未连接，控制信号（DB" + db + ".DBW" + byteOffset + "）未下发");
            return;
        }
        plcService.writeWord(db, byteOffset, value);
    }

    /** 采样间隔变更：写回配置并重启轮询，使新间隔立即生效（未连接或手动模式仅记录配置）。 */
    private void refreshMeterInterval() {
        long interval = currentIntervalMs();
        cfg.setPollIntervalMs(interval);
        if (meterService != null) meterService.getConfig().setPollIntervalMs(interval);
        if (footerBarController != null) footerBarController.setConfig(cfg.getComPort(), interval);
        appendLog(LogType.INFO, "采样间隔已更新为 " + (interval / 1000.0) + "秒/次");
        if (meterService == null) return;
        if (rightPanelController != null && !rightPanelController.isAuto()) {
            meterService.stopPolling();
            appendLog(LogType.WARNING, "自动采集未启用，新间隔将在启用后生效");
            return;
        }
        meterService.stopPolling();
        meterService.startPolling();
    }

    /** 头部「自动 / 手动模式」切换：全局唯一模式入口，同步右面板与轮询。 */
    private void applyAutoMode(boolean auto) {
        if (rightPanelController != null) rightPanelController.setAutoMode(auto);
        if (meterService == null) return;
        if (auto) {
            meterService.startPolling();
            appendLog(LogType.INFO, "已切换为自动模式，恢复定时采集");
        } else {
            meterService.stopPolling();
            appendLog(LogType.WARNING, "已切换为手动模式，仅保留「单次获取」");
        }
    }

    /** 点击底部「电阻计(COMx)」状态手动重连。 */
    private void reconnectMeter() {
        if (meterService == null) return;
        appendLog(LogType.COM, "手动请求重连电阻计 " + cfg.getComPort() + " @" + cfg.getBaudRate() + "bps ...");
        meterService.connect();
    }

    /** 每秒心跳：设备断线时按周期自动重连（手动模式下不自动重连）。 */
    private void tickReconnect() {
        if (meterService == null) return;
        if (meterService.isConnected()) {
            reconnectCountdown = RECONNECT_PERIOD_SECONDS;
            return;
        }
        if (rightPanelController != null && !rightPanelController.isAuto()) return;
        if (--reconnectCountdown > 0) return;
        reconnectCountdown = RECONNECT_PERIOD_SECONDS;
        appendLog(LogType.COM, "尝试重新连接电阻计 " + cfg.getComPort() + " ...");
        meterService.connect();
    }

    /* ===== MeasurementListener 回调（rmcs-meter 后台线程） ===== */
    @Override
    public void onConnected() {
        Platform.runLater(() -> {
            if (footerBarController != null) footerBarController.setMeterOnline(true);
            appendLog(LogType.COM, "电阻计连接成功");
        });
    }

    @Override
    public void onConnectionFailed(String reason) {
        Platform.runLater(() -> {
            if (footerBarController != null) footerBarController.setMeterOnline(false);
            appendLog(LogType.ERROR, "电阻计连接失败: " + reason);
        });
    }

    @Override
    public void onDisconnected(String reason) {
        Platform.runLater(() -> {
            if (footerBarController != null) footerBarController.setMeterOnline(false);
            appendLog(LogType.ERROR, "电阻计已断线: " + reason);
        });
    }

    @Override
    public void onReadError(String reason) {
        Platform.runLater(() -> appendLog(LogType.WARNING, "读取失败: " + reason));
    }

    @Override
    public void onMeasurement(Measurement m) {
        if (m == null) return;
        if (!m.isValid()) {
            Platform.runLater(() -> appendLog(LogType.WARNING, "电表返回值无效: " + m.getRaw()));
            return;
        }
        // 仅缓存最近一次有效读数，真正写履历由 PLC 数据更新触发（位置来自 PLC）
        lastMeterValue = m.getValue();
    }

    @Override
    public void onLog(String message) {
        Platform.runLater(() -> appendLog(LogType.COM, message));
    }

    public void appendLog(LogType type, String msg) {
        LogEntry e = new LogEntry(String.valueOf(System.nanoTime()), type, msg);
        ObservableList<LogEntry> logs = sharedLogs;
        Platform.runLater(() -> {
            if (logs.size() > MAX_LOGS) logs.remove(MAX_LOGS, logs.size());
            logs.add(0, e);
        });
    }

    /* ===== 人员操作 ===== */
    public void startMachine() {
        status.setStatus(ProductionStatus.MachineStatus.RUNNING);
        if (rightPanelController != null) rightPanelController.refreshStatus();
        setMatrixLive(status.getStatus());
        plcWrite(cfg.getPlcControlDb(), cfg.getPlcStartOffset(), 1); // 设备启动
        appendLog(LogType.PLC, "启动自动测量循环（已下发 PLC 启动信号）");
    }

    public void stopMachine() {
        status.setStatus(ProductionStatus.MachineStatus.STOPPED);
        if (rightPanelController != null) rightPanelController.refreshStatus();
        setMatrixLive(status.getStatus());
        plcWrite(cfg.getPlcControlDb(), cfg.getPlcStopOffset(), 1); // 设备停止
        appendLog(LogType.WARNING, "测量已停止（已下发 PLC 停止信号）");
    }

    public void resetMachine() {
        status.setStatus(ProductionStatus.MachineStatus.RESETTING);
        status.resetCounters();
        lastMeterValue = null;
        currentProductName = "";
        if (workMatrixY1Controller != null) workMatrixY1Controller.setProductName("");
        if (workMatrixY2Controller != null) workMatrixY2Controller.setProductName("");
        clearHistory();
        clearMatrix();
        allRecords.clear();
        plcWrite(cfg.getPlcControlDb(), cfg.getPlcResetOffset(), 1); // 设备复位
        if (rightPanelController != null) rightPanelController.refreshStatus();
        appendLog(LogType.PLC, "系统已复位（已下发 PLC 复位信号）");
        Platform.runLater(() -> status.setStatus(ProductionStatus.MachineStatus.STANDYBY));
        if (rightPanelController != null) rightPanelController.refreshStatus();
        setMatrixLive(status.getStatus());
    }

    public void captureOnce() {
        if (meterService == null) return;
        if (meterService.isConnected()) {
            meterService.stopPolling();
            meterService.startPolling();
            appendLog(LogType.COM, "请求单次采集（已重启轮询）");
        } else {
            meterService.connect();
            appendLog(LogType.COM, "请求单次采集（重新连接）");
        }
    }

    /* ===== 模态窗口 ===== */

    /** 加载模态 FXML 并以独立 Stage 显示，返回其 Controller。 */
    private <T> T showModal(String fxml, String title, double w, double h, java.util.function.Consumer<T> setup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modals/" + fxml));
            Parent root = loader.load();
            // 跟随主场景当前主题，而不是依赖可能不同步的本地布尔值
            boolean isLight = true;
            if (app != null && app.getStage() != null && app.getStage().getScene() != null) {
                Parent mainRoot = app.getStage().getScene().getRoot();
                isLight = mainRoot.getStyleClass().contains("root-light")
                        || !mainRoot.getStyleClass().contains("root-dark");
            }
            root.getStyleClass().add(isLight ? "root-light" : "root-dark");
            T c = loader.getController();
            if (setup != null) setup.accept(c);
            Stage stage = new Stage();
            // 透明窗口：场景/遮罩层全透明，仅显示圆角卡片本体（UNDECORATED +
            // 默认白底场景会从 modal-card 的圆角外透出，形成四角灰边）
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            stage.initModality(Modality.APPLICATION_MODAL);
            if (app != null && app.getStage() != null) {
                stage.initOwner(app.getStage());
            }
            stage.setResizable(false);
            javafx.scene.Scene modalScene = new Scene(root, w, h);
            modalScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            root.setStyle("-fx-background-color: transparent;");
            stage.setScene(modalScene);
            // 弹窗独立场景：同步主窗口当前主题的内联着色（保底方案）
            if (app != null) {
                com.rmcs.util.ThemeStyler.apply(stage.getScene(), app.isDarkTheme());
            }
            stage.centerOnScreen();
            // 无边框窗口：整窗可拖拽移动
            final double[] drag = new double[2];
            root.setOnMousePressed(e -> {
                drag[0] = e.getSceneX();
                drag[1] = e.getSceneY();
            });
            root.setOnMouseDragged(e -> {
                stage.setX(e.getScreenX() - drag[0]);
                stage.setY(e.getScreenY() - drag[1]);
            });
            stage.show();
            // 表头等 skin 节点在 show() 之后才创建，必须在 show 后补一轮着色
            if (app != null) {
                javafx.scene.Scene styledScene = stage.getScene();
                Platform.runLater(() ->
                        com.rmcs.util.ThemeStyler.apply(styledScene, app.isDarkTheme()));
                new javafx.animation.Timeline(new javafx.animation.KeyFrame(
                        javafx.util.Duration.millis(400),
                        e -> com.rmcs.util.ThemeStyler.apply(styledScene, app.isDarkTheme()))).play();
            }
            return c;
        } catch (Exception e) {
            appendLog(LogType.ERROR, "打开" + title + "失败: " + e.getMessage());
            return null;
        }
    }

    public void openStandardConfig() {
        showModal("standard_config.fxml", "参数设定", 620, 640, (StandardConfigController c) -> {
            c.bind(cfg);
            c.setOnSaved(() -> {
                boolean portChanged = meterService != null
                        && (!cfg.getComPort().equalsIgnoreCase(meterService.getConfig().getPortName())
                            || cfg.getBaudRate() != meterService.getConfig().getBaudRate());
                if (meterService != null) {
                    SerialConfig sc = meterService.getConfig();
                    sc.setStandardValue(cfg.getStandard().getStandardValue());
                    sc.setUpperDev(cfg.getStandard().getUpperDev());
                    sc.setLowerDev(cfg.getStandard().getLowerDev());
                }
                // PLC 参数是否变化（含 IP / 机架 / 槽号 / DB 号 / 所有偏移 / 模拟开关）
                boolean plcChanged = plcService == null || !plcService.matchesConfig(cfg);
                if (rightPanelController != null) {
                    rightPanelController.refreshStandard();
                    rightPanelController.refreshStatus();
                }
                if (footerBarController != null) {
                    footerBarController.setConfig(cfg.getComPort(), cfg.getPollIntervalMs());
                }
                appendLog(LogType.INFO, String.format(
                        "参数已保存 · 标准=%.3fΩ 区间=[%.3f, %.3f] 串口=%s %dbps 间隔=%dms",
                        cfg.getStandard().getStandardValue(),
                        cfg.getStandard().getLowerLimit(), cfg.getStandard().getUpperLimit(),
                        cfg.getComPort(), cfg.getBaudRate(), cfg.getPollIntervalMs()));
                if (portChanged) {
                    appendLog(LogType.WARNING, "串口参数已变更，正在按 " + cfg.getComPort()
                            + " @" + cfg.getBaudRate() + "bps 重新连接...");
                    meterService.shutdown();
                    startMeterService();
                }
                if (plcChanged) {
                    appendLog(LogType.WARNING, "PLC 参数已变更，正在重新连接 "
                            + (cfg.isPlcMock() ? "(离线模拟)" : cfg.getPlcIp()) + " ...");
                    if (plcService != null) plcService.shutdown();
                    startPlcService();
                }
            });
        });
    }

    public void openHistoryQuery() {
        showModal("history_query.fxml", "履历查询", 780, 540, (HistoryQueryController c) -> {
            c.bind(allRecords);
            c.setOnCleared(() -> {
                allRecords.clear();
                clearHistory();
            });
        });
    }

    public void openLogQuery() {
        showModal("log_query.fxml", "日志查询", 800, 540, (LogQueryController c) -> {
            c.bind(sharedLogs);
            c.setOnCleared(sharedLogs::clear);
        });
    }

    /** 直接导出全部检测履历为 CSV（右面板「导出数据」按钮）。 */
    public void exportCsv() {
        var records = allRecords;
        FileChooser fc = new FileChooser();
        fc.setTitle("导出检测履历");
        fc.setInitialFileName("inspection_history_" + timestamp() + ".csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV 文件", "*.csv"));
        File file = fc.showSaveDialog(ownerWindow());
        if (file == null) return;
        try (PrintWriter w = new PrintWriter(file, "UTF-8")) {
            w.write('\uFEFF');
            w.println("时间,Y1/Y2,列号,行号,左/右,产品名称,实测值,标准值,判定");
            for (com.rmcs.model.InspectionRecord r : records) {
                w.printf("%s,%s,%d,%s,%s,%s,%s,%s,%s%n", r.getFormattedTime(), r.getSide(), r.getCol(),
                        com.rmcs.model.WorkPos.rowName(r.getRow()), r.getLr(), r.getProductName(),
                        r.getFormattedMeasured(), r.getFormattedStandard(), r.isResult() ? "合格" : "不合格");
            }
            w.flush();
            appendLog(LogType.SUCCESS, "已导出 " + records.size() + " 条履历 → " + file.getName());
        } catch (Exception e) {
            appendLog(LogType.ERROR, "导出失败: " + e.getMessage());
        }
    }

    /** 导出文件名时间戳 yyyyMMdd_HHmmss。 */
    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private javafx.stage.Window ownerWindow() {
        return app == null ? null : app.getStage();
    }

    public void logout() {
        releaseResources();
        if (app != null) app.showLogin();
    }

    /** 释放后台线程、串口与计时器；登出或关闭窗口时调用，避免重复登录累积泄漏。 */
    public void releaseResources() {
        if (meterService != null) { meterService.shutdown(); meterService = null; }
        if (plcService != null) { plcService.shutdown(); plcService = null; }
        if (statusTick != null) { statusTick.stop(); statusTick = null; }
        if (footerBarController != null) footerBarController.stop();
    }

    /* ===== 测试钩子 ===== */
    public AuthUser getCurrentUser()         { return user; }
    public SystemConfig getSystemConfig()    { return cfg; }
    public ProductionStatus getProductionStatus() { return status; }
    public MeterService getMeterService()    { return meterService; }
}