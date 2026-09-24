package com.rmcs.controller;

import com.rmcs.model.ProductionStatus;
import com.rmcs.model.ProductionStatus.MachineStatus;
import com.rmcs.model.SystemConfig;
import com.rmcs.model.TestStandard;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

/** 右侧控制面板：测定标准 / 数据采集 / 运行状况 / 人员操作。 */
public class RightPanelController implements Initializable {

    @FXML private Label lblStandard, lblUpper, lblLower, lblRange;
    @FXML private Label lblCount, lblStatus, lblDuration;
    @FXML private ToggleButton tb05, tb10, tb14, tb20, tb30;
    @FXML private ToggleGroup intervalGroup;
    @FXML private TextField tfCustomInterval;
    @FXML private Label lblIntervalInfo;
    @FXML private Label lblRxTx, lblStatusSub, lblDurationSec;
    @FXML private Button btnStart, btnStop, btnCapture;
    @FXML private Label lblStartTitle;

    private SystemConfig cfg;
    private ProductionStatus status;
    private long intervalMs = 1400L;
    /** 采集模式（由顶栏「自动/手动模式」统一控制）。 */
    private boolean autoMode = true;

    private Runnable onStandardConfig, onHistoryQuery, onLogQuery, onExportCsv;
    private Runnable onStart, onStop, onReset, onCapture;
    private Runnable onIntervalChange, onResetCounter;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tb05.setUserData(500L);
        tb10.setUserData(1000L);
        tb14.setUserData(1400L);
        tb20.setUserData(2000L);
        tb30.setUserData(3000L);
        tb14.setSelected(true);
        refreshIntervalInfo();
        refreshAcquisition();

        intervalGroup.selectedToggleProperty().addListener((obs, old, t) -> {
            if (t == null) {
                // 不允许全部取消：恢复默认档位
                tb14.setSelected(true);
                return;
            }
            Object val = t.getUserData();
            if (val instanceof Long) {
                intervalMs = (Long) val;
                refreshIntervalInfo();
                fireIntervalChange();
            }
        });
    }

    private void refreshIntervalInfo() {
        if (lblIntervalInfo != null) {
            lblIntervalInfo.setText(String.format("%d ms (%.1fs/次)", intervalMs, intervalMs / 1000.0));
        }
    }

    public void bind(SystemConfig cfg, ProductionStatus status) {
        this.cfg = cfg;
        this.status = status;
        refreshStandard();
        refreshStatus();
    }

    public void refreshStandard() {
        if (cfg == null || cfg.getStandard() == null) return;
        TestStandard s = cfg.getStandard();
        lblStandard.setText(String.format("%.3f", s.getStandardValue()));
        lblUpper.setText(String.format("+%.2f", s.getUpperDev()));
        lblLower.setText(String.format("-%.2f", s.getLowerDev()));
        lblRange.setText(String.format("[%.3f Ω ~ %.3f Ω]", s.getLowerLimit(), s.getUpperLimit()));
    }

    /** 采集状态文案：RX/TX 指示（原型三态：RX数据采样 / 轮询就绪 / 手动模式）。 */
    private void refreshAcquisition() {
        boolean auto = isAuto();
        if (lblRxTx != null) {
            String text;
            String cls;
            if (!auto) {
                text = "● 手动模式";
                cls = "txt-secondary";
            } else if (status != null && status.getStatus() == MachineStatus.RUNNING) {
                text = "● RX数据采样";
                cls = "txt-success";
            } else {
                text = "● 轮询就绪";
                cls = "txt-secondary";
            }
            lblRxTx.setText(text);
            lblRxTx.getStyleClass().removeAll("txt-success", "txt-secondary");
            lblRxTx.getStyleClass().add(cls);
        }
    }

    public void refreshStatus() {
        if (status == null || lblStatus == null) return;
        lblCount.setText(String.format("%,d", status.getCompletedCount()));
        String label;
        String cls;
        switch (status.getStatus()) {
            case RUNNING:   label = "● 运行中"; cls = "badge-running"; break;
            case STANDYBY:  label = "● 待机";   cls = "badge-info";    break;
            case STOPPED:   label = "● 已停止"; cls = "badge-danger";  break;
            case RESETTING: label = "● 复位中"; cls = "badge-warning"; break;
            default:        label = "—";        cls = "badge-info";
        }
        lblStatus.setText(label);
        lblStatus.getStyleClass().removeAll("badge-running", "badge-info", "badge-danger", "badge-warning");
        lblStatus.getStyleClass().add(cls);
        lblDuration.setText(status.getFormattedDuration());
        if (lblDurationSec != null) {
            lblDurationSec.setText(status.getRunDurationSeconds() + " 秒");
        }
        if (lblStatusSub != null) {
            lblStatusSub.setText(status.getStatus() == MachineStatus.RUNNING ? "巡检中" : "就绪");
        }
        if (lblStartTitle != null) {
            // 按钮采用自定义图形（图标 + 双行标签），运行状态更新图形内的主标签，
            // 不再调用 btnStart.setText（会与图形并排渲染导致溢出）
            lblStartTitle.setText(status.getStatus() == MachineStatus.RUNNING ? "设备运行中" : "设备启动");
        }
        refreshAcquisition();
    }

    public MachineStatus getMachineStatus() {
        return status == null ? MachineStatus.STANDYBY : status.getStatus();
    }

    /** 供主控制台做首帧布局自检：返回本面板所在 Scene。 */
    public javafx.scene.Scene getScene() {
        return btnStart == null ? null : btnStart.getScene();
    }

    public boolean isAuto() {
        return autoMode;
    }

    /** 由顶栏「自动 / 手动模式」切换时同步（自动获取数据跟随全局模式）。 */
    public void setAutoMode(boolean auto) {
        this.autoMode = auto;
        refreshAcquisition();
    }

    public long getIntervalMs() {
        return intervalMs;
    }

    public void setOnStandardConfig(Runnable r) { onStandardConfig = r; }
    public void setOnHistoryQuery(Runnable r) { onHistoryQuery = r; }
    public void setOnLogQuery(Runnable r) { onLogQuery = r; }
    public void setOnExport(Runnable r) { onExportCsv = r; }
    public void setOnStart(Runnable r) { onStart = r; }
    public void setOnStop(Runnable r) { onStop = r; }
    public void setOnReset(Runnable r) { onReset = r; }
    public void setOnCapture(Runnable r) { onCapture = r; }
    public void setOnIntervalChange(Runnable r) { onIntervalChange = r; }
    public void setOnResetCounter(Runnable r) { onResetCounter = r; }

    @FXML public void resetCounter() { if (onResetCounter != null) onResetCounter.run(); }

    @FXML public void openStandardConfig() { if (onStandardConfig != null) onStandardConfig.run(); }
    @FXML public void openHistoryQuery()   { if (onHistoryQuery != null) onHistoryQuery.run(); }
    @FXML public void openLogQuery()       { if (onLogQuery != null) onLogQuery.run(); }
    @FXML public void exportCsv()          { if (onExportCsv != null) onExportCsv.run(); }
    @FXML public void startMachine()       { if (onStart != null) onStart.run(); }
    @FXML public void stopMachine()        { if (onStop != null) onStop.run(); }
    @FXML public void resetMachine()       { if (onReset != null) onReset.run(); }
    @FXML public void captureOnce()        { if (onCapture != null) onCapture.run(); }
    @FXML public void applyCustom() {
        try {
            long ms = Long.parseLong(tfCustomInterval.getText().trim());
            if (ms >= 100) {
                intervalGroup.getToggles().forEach(t -> t.setSelected(false));
                intervalMs = ms;
                refreshIntervalInfo();
                fireIntervalChange();
            }
        } catch (Exception ignored) {
        }
    }

    private void fireIntervalChange() {
        if (onIntervalChange != null) onIntervalChange.run();
    }
}