package com.rmcs.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * 底部条：PLC 通讯 / 电阻计(COMx) 串口状态 / 波特率 / 平台名 / 时钟。
 * 电阻计状态标签可点击，触发串口重连。
 */
public class FooterBarController implements Initializable {

    @FXML private Label lblPlcStatus;
    @FXML private Label lblMeterStatus;
    @FXML private Label lblBaud;
    @FXML private Label lblClock;

    private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private Timeline tickTimeline;
    private String comPort = "COM4";
    private boolean plcOnline = true;
    private boolean meterOnline = false;
    private Runnable onReconnect;
    private Runnable onPlcReconnect;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tickTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                lblClock.setText(LocalDateTime.now().format(FMT))));
        tickTimeline.setCycleCount(Timeline.INDEFINITE);
        tickTimeline.play();

        lblMeterStatus.setOnMouseClicked(e -> {
            if (onReconnect != null) onReconnect.run();
        });
        // PLC 状态可点击：手动重连 PLC（S7 通讯）
        if (lblPlcStatus != null) {
            lblPlcStatus.setCursor(javafx.scene.Cursor.HAND);
            lblPlcStatus.setTooltip(new Tooltip("点击重新连接 PLC"));
            lblPlcStatus.setOnMouseClicked(e -> {
                if (onPlcReconnect != null) onPlcReconnect.run();
            });
        }
    }

    /** 点击 PLC 状态触发 S7 重连。 */
    public void setOnPlcReconnect(Runnable onPlcReconnect) {
        this.onPlcReconnect = onPlcReconnect;
    }

    /** 点击电阻计状态触发重连。 */
    public void setOnReconnect(Runnable onReconnect) {
        this.onReconnect = onReconnect;
    }

    public void setConfig(String comPort, long intervalMs) {
        if (comPort != null && !comPort.isBlank()) {
            this.comPort = comPort;
            refreshMeterLabel();
        }
    }

    public void setBaudRate(int baudRate) {
        if (lblBaud != null) {
            lblBaud.setText(baudRate + "bps 8-N-1");
        }
    }

    public void setPlcOnline(boolean online) {
        this.plcOnline = online;
        applyStatus(lblPlcStatus, online, "PLC通讯");
    }

    public void setMeterOnline(boolean online) {
        this.meterOnline = online;
        refreshMeterLabel();
    }

    private void refreshMeterLabel() {
        applyStatus(lblMeterStatus, meterOnline, "电阻计(" + comPort + ")");
    }

    private void applyStatus(Label lbl, boolean online, String name) {
        if (lbl == null) return;
        // 原型文案：PLC通信 已连接/中断；电阻计(COMx) 就绪/脱机
        boolean isPlc = name.startsWith("PLC");
        String ok = isPlc ? "已连接" : "就绪";
        String bad = isPlc ? "中断" : "脱机";
        lbl.setText(online ? "● " + name + ": " + ok : "● " + name + ": " + bad);
        lbl.getStyleClass().removeAll("footer-status-online", "footer-status-offline");
        lbl.getStyleClass().add(online ? "footer-status-online" : "footer-status-offline");
    }

    public void stop() {
        if (tickTimeline != null) tickTimeline.stop();
    }
}
