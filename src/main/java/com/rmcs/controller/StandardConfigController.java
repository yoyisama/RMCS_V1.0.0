package com.rmcs.controller;

import com.rmcs.model.SystemConfig;
import javafx.fxml.FXML;
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
    @FXML private ComboBox<String> cbComPort;
    @FXML private ComboBox<String> cbBaud;
    @FXML private Label lblRange;
    @FXML private Label lblError;

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
        tfPlcIp.setText("192.168.1.10");
        tfInterval.setText("60000");
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

        if (cfg != null) {
            cfg.getStandard().setStandardValue(std);
            cfg.getStandard().setUpperDev(up);
            cfg.getStandard().setLowerDev(low);
            cfg.setComPort(cbComPort.getValue());
            try { cfg.setBaudRate(Integer.parseInt(cbBaud.getValue())); } catch (Exception ignored) { }
            cfg.setPlcIp(tfPlcIp.getText());
            cfg.setPollIntervalMs(interval);
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
