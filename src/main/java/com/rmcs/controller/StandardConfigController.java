package com.rmcs.controller;

import com.rmcs.model.SystemConfig;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** 参数设定模态：编辑测定标准与通讯参数，保存后回调主控制台。 */
public class StandardConfigController {

    @FXML private TextField tfStandard;
    @FXML private TextField tfUpperDev;
    @FXML private TextField tfLowerDev;
    @FXML private TextField tfPlcIp;
    @FXML private TextField tfInterval;
    @FXML private TextField tfPlcRack;
    @FXML private TextField tfPlcSlot;
    @FXML private TextField tfPlcLen;
    @FXML private TextField tfPlcModel;
    @FXML private TextField tfPlcPort;
    @FXML private CheckBox chkPlcMock;
    @FXML private ComboBox<String> cbComPort;
    @FXML private ComboBox<String> cbBaud;
    @FXML private Label lblRange;
    @FXML private Label lblError;

    // PLC 数据区 (DB) 配置
    @FXML private TextField tfPlcDataDb;
    @FXML private TextField tfPlcHeartbeatOffset;
    @FXML private TextField tfPlcNameOffset;
    @FXML private TextField tfPlcUpdateFlagOffset;
    @FXML private TextField tfPlcSideOffset;
    @FXML private TextField tfPlcRowOffset;
    @FXML private TextField tfPlcColOffset;
    @FXML private TextField tfPlcYCodeOffset;

    // PLC 控制区 (DB) 配置
    @FXML private TextField tfPlcControlDb;
    @FXML private TextField tfPlcAckOffset;
    @FXML private TextField tfPlcStartOffset;
    @FXML private TextField tfPlcStopOffset;
    @FXML private TextField tfPlcResetOffset;

    // 作业矩阵规模
    @FXML private TextField tfMatrixRows;
    @FXML private TextField tfMatrixCols;

    private SystemConfig cfg;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        cbComPort.getItems().addAll("COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8");
        cbBaud.getItems().addAll("2400", "4800", "9600", "19200", "38400", "57600", "115200");
        tfStandard.textProperty().addListener(o -> refreshRange());
        tfUpperDev.textProperty().addListener(o -> refreshRange());
        tfLowerDev.textProperty().addListener(o -> refreshRange());
    }

    public void bind(SystemConfig cfg) {
        this.cfg = cfg;
        if (cfg == null) return;
        tfStandard.setText(fmt(cfg.getStandard().getStandardValue(), 3));
        tfUpperDev.setText(fmt(cfg.getStandard().getUpperDev(), 3));
        tfLowerDev.setText(fmt(cfg.getStandard().getLowerDev(), 3));
        cbComPort.setValue(cfg.getComPort());
        cbBaud.setValue(String.valueOf(cfg.getBaudRate()));
        tfPlcIp.setText(cfg.getPlcIp());
        tfInterval.setText(String.valueOf(cfg.getPollIntervalMs()));
        tfPlcRack.setText(String.valueOf(cfg.getPlcRack()));
        tfPlcSlot.setText(String.valueOf(cfg.getPlcSlot()));
        tfPlcLen.setText(String.valueOf(cfg.getPlcProductLen()));
        tfPlcModel.setText(cfg.getPlcModel());
        tfPlcPort.setText(String.valueOf(cfg.getPlcPort()));
        chkPlcMock.setSelected(cfg.isPlcMock());

        tfPlcDataDb.setText(String.valueOf(cfg.getPlcDataDb()));
        tfPlcHeartbeatOffset.setText(String.valueOf(cfg.getPlcHeartbeatOffset()));
        tfPlcNameOffset.setText(String.valueOf(cfg.getPlcNameOffset()));
        tfPlcUpdateFlagOffset.setText(String.valueOf(cfg.getPlcUpdateFlagOffset()));
        tfPlcSideOffset.setText(String.valueOf(cfg.getPlcSideOffset()));
        tfPlcRowOffset.setText(String.valueOf(cfg.getPlcRowOffset()));
        tfPlcColOffset.setText(String.valueOf(cfg.getPlcColOffset()));
        tfPlcYCodeOffset.setText(String.valueOf(cfg.getPlcYCodeOffset()));

        tfPlcControlDb.setText(String.valueOf(cfg.getPlcControlDb()));
        tfPlcAckOffset.setText(String.valueOf(cfg.getPlcAckOffset()));
        tfPlcStartOffset.setText(String.valueOf(cfg.getPlcStartOffset()));
        tfPlcStopOffset.setText(String.valueOf(cfg.getPlcStopOffset()));
        tfPlcResetOffset.setText(String.valueOf(cfg.getPlcResetOffset()));

        tfMatrixRows.setText(String.valueOf(cfg.getMatrixRows()));
        tfMatrixCols.setText(String.valueOf(cfg.getMatrixCols()));

        refreshRange();
    }

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }

    @FXML
    public void resetDefault() {
        tfStandard.setText("6.532");
        tfUpperDev.setText("0.010");
        tfLowerDev.setText("0.010");
        cbComPort.setValue("COM4");
        cbBaud.setValue("9600");
        tfPlcIp.setText("192.168.0.8");
        tfInterval.setText("60000");
        tfPlcRack.setText("0");
        tfPlcSlot.setText("1");
        tfPlcLen.setText("256");
        tfPlcModel.setText("S7-1200");
        tfPlcPort.setText("102");
        chkPlcMock.setSelected(false);

        tfPlcDataDb.setText("36");
        tfPlcHeartbeatOffset.setText("0");
        tfPlcNameOffset.setText("2");
        tfPlcUpdateFlagOffset.setText("258");
        tfPlcSideOffset.setText("260");
        tfPlcRowOffset.setText("262");
        tfPlcColOffset.setText("264");
        tfPlcYCodeOffset.setText("266");

        tfPlcControlDb.setText("37");
        tfPlcAckOffset.setText("0");
        tfPlcStartOffset.setText("6");
        tfPlcStopOffset.setText("2");
        tfPlcResetOffset.setText("4");

        tfMatrixRows.setText("10");
        tfMatrixCols.setText("3");

        refreshRange();
    }

    @FXML
    public void close() {
        Stage s = stage();
        if (s != null) s.close();
    }

    @FXML
    public void save() {
        double std = parse(tfStandard.getText(), Double.NaN);
        double up = parse(tfUpperDev.getText(), Double.NaN);
        double low = parse(tfLowerDev.getText(), Double.NaN);
        long interval = (long) parse(tfInterval.getText(), Double.NaN);
        int rack = (int) parse(tfPlcRack.getText(), 0);
        int slot = (int) parse(tfPlcSlot.getText(), 0);
        int plen = (int) parse(tfPlcLen.getText(), 256);
        int port = (int) parse(tfPlcPort.getText(), 102);

        int dataDb = (int) parse(tfPlcDataDb.getText(), 36);
        int heartbeatOff = (int) parse(tfPlcHeartbeatOffset.getText(), 0);
        int nameOff = (int) parse(tfPlcNameOffset.getText(), 2);
        int updateFlagOff = (int) parse(tfPlcUpdateFlagOffset.getText(), 258);
        int sideOff = (int) parse(tfPlcSideOffset.getText(), 260);
        int rowOff = (int) parse(tfPlcRowOffset.getText(), 262);
        int colOff = (int) parse(tfPlcColOffset.getText(), 264);
        int yCodeOff = (int) parse(tfPlcYCodeOffset.getText(), 266);

        int controlDb = (int) parse(tfPlcControlDb.getText(), 37);
        int ackOff = (int) parse(tfPlcAckOffset.getText(), 0);
        int startOff = (int) parse(tfPlcStartOffset.getText(), 6);
        int stopOff = (int) parse(tfPlcStopOffset.getText(), 2);
        int resetOff = (int) parse(tfPlcResetOffset.getText(), 4);

        int mRows = (int) parse(tfMatrixRows.getText(), 10);
        int mCols = (int) parse(tfMatrixCols.getText(), 3);

        if (Double.isNaN(std) || Double.isNaN(up) || Double.isNaN(low)) {
            showError("标准值与上下偏差必须为有效数字");
            return;
        }
        if (up < 0 || low < 0) {
            showError("偏差不能为负数");
            return;
        }
        if (Double.isNaN(interval) || interval < 200) {
            showError("采样间隔必须为 ≥200 的整数（毫秒）");
            return;
        }
        if (plen < 2 || plen > 256) {
            showError("产品名最大长度需在 2~256 字节之间");
            return;
        }
        if (port < 1 || port > 65535) {
            showError("PLC 端口需在 1~65535 之间（S7 默认 102）");
            return;
        }
        if (dataDb < 1 || dataDb > 65535 || controlDb < 1 || controlDb > 65535) {
            showError("DB 号需在 1~65535 之间");
            return;
        }
        if (nameOff < 0 || heartbeatOff < 0 || updateFlagOff < 0 || sideOff < 0
                || rowOff < 0 || colOff < 0 || yCodeOff < 0) {
            showError("数据区偏移量不能为负数");
            return;
        }
        if (ackOff < 0 || startOff < 0 || stopOff < 0 || resetOff < 0) {
            showError("控制区偏移量不能为负数");
            return;
        }
        if (mRows < 1 || mRows > 30 || mCols < 1 || mCols > 10) {
            showError("矩阵行数需 1~30、列数需 1~10");
            return;
        }

        if (cfg != null) {
            cfg.getStandard().setStandardValue(std);
            cfg.getStandard().setUpperDev(up);
            cfg.getStandard().setLowerDev(low);
            cfg.setComPort(cbComPort.getValue());
            try { cfg.setBaudRate(Integer.parseInt(cbBaud.getValue())); } catch (Exception ignored) { }
            cfg.setPlcIp(tfPlcIp.getText());
            cfg.setPlcPort(port);
            cfg.setPollIntervalMs(interval);
            cfg.setPlcRack(rack);
            cfg.setPlcSlot(slot);
            cfg.setPlcProductLen(plen);
            cfg.setPlcMock(chkPlcMock.isSelected());

            cfg.setPlcDataDb(dataDb);
            cfg.setPlcHeartbeatOffset(heartbeatOff);
            cfg.setPlcNameOffset(nameOff);
            cfg.setPlcUpdateFlagOffset(updateFlagOff);
            cfg.setPlcSideOffset(sideOff);
            cfg.setPlcRowOffset(rowOff);
            cfg.setPlcColOffset(colOff);
            cfg.setPlcYCodeOffset(yCodeOff);

            cfg.setPlcControlDb(controlDb);
            cfg.setPlcAckOffset(ackOff);
            cfg.setPlcStartOffset(startOff);
            cfg.setPlcStopOffset(stopOff);
            cfg.setPlcResetOffset(resetOff);

            cfg.setMatrixRows(mRows);
            cfg.setMatrixCols(mCols);
        }
        if (onSaved != null) onSaved.run();
        close();
    }

    private void refreshRange() {
        double std = parse(tfStandard.getText(), Double.NaN);
        double up = parse(tfUpperDev.getText(), 0);
        double low = parse(tfLowerDev.getText(), 0);
        if (Double.isNaN(std)) { lblRange.setText("-"); return; }
        lblRange.setText(String.format("%.3f ~ %.3f Ω", std - low, std + up));
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private Stage stage() {
        if (lblRange == null || lblRange.getScene() == null) return null;
        return (Stage) lblRange.getScene().getWindow();
    }

    private static double parse(String s, double def) {
        try { return Double.parseDouble(s == null ? "" : s.trim()); } catch (Exception e) { return def; }
    }

    private static String fmt(double v, int digits) {
        return String.format("%." + digits + "f", v);
    }
}
