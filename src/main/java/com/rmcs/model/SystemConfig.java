package com.rmcs.model;

/** 系统配置（UI 层）：串口/PLC IP/采样间隔/自动开关等。 */
public class SystemConfig {

    private TestStandard standard = new TestStandard();
    private String comPort = "COM4";
    private int baudRate = 9600;
    private String plcIp = "192.168.1.10";
    private int plcRack = 0;
    private int plcSlot = 0;
    private int plcProductLen = 256;
    private int plcPollMs = 200;
    private boolean plcMock = false;
    // PLC 型号（当前固定 S7-1200，保留字段便于后续扩展）
    private String plcModel = "S7-1200";
    // PLC → 上位机 数据区
    private int plcDataDb = 36;
    private int plcHeartbeatOffset = 0;
    private int plcNameOffset = 2;
    private int plcUpdateFlagOffset = 258;
    private int plcSideOffset = 260;
    private int plcRowOffset = 262;
    private int plcColOffset = 264;
    private int plcYCodeOffset = 266;
    // 上位机 → PLC 控制区
    private int plcControlDb = 37;
    private int plcAckOffset = 0;
    private int plcStartOffset = 6;
    private int plcStopOffset = 2;
    private int plcResetOffset = 4;
    private long pollIntervalMs = 60_000L;
    private boolean autoFetch = true;

    public TestStandard getStandard() { return standard; }
    public void setStandard(TestStandard standard) { this.standard = standard; }

    public String getComPort() { return comPort; }
    public void setComPort(String comPort) { this.comPort = comPort; }

    public int getBaudRate() { return baudRate; }
    public void setBaudRate(int baudRate) { this.baudRate = baudRate; }

    public String getPlcIp() { return plcIp; }
    public void setPlcIp(String plcIp) { this.plcIp = plcIp; }

    public int getPlcRack() { return plcRack; }
    public void setPlcRack(int plcRack) { this.plcRack = plcRack; }

    public int getPlcSlot() { return plcSlot; }
    public void setPlcSlot(int plcSlot) { this.plcSlot = plcSlot; }

    public int getPlcProductLen() { return plcProductLen; }
    public void setPlcProductLen(int plcProductLen) { this.plcProductLen = plcProductLen; }

    public int getPlcPollMs() { return plcPollMs; }
    public void setPlcPollMs(int plcPollMs) { this.plcPollMs = plcPollMs; }

    public boolean isPlcMock() { return plcMock; }
    public void setPlcMock(boolean plcMock) { this.plcMock = plcMock; }

    public String getPlcModel() { return plcModel; }
    public void setPlcModel(String plcModel) { this.plcModel = plcModel; }

    public int getPlcDataDb() { return plcDataDb; }
    public void setPlcDataDb(int plcDataDb) { this.plcDataDb = plcDataDb; }
    public int getPlcHeartbeatOffset() { return plcHeartbeatOffset; }
    public void setPlcHeartbeatOffset(int plcHeartbeatOffset) { this.plcHeartbeatOffset = plcHeartbeatOffset; }
    public int getPlcNameOffset() { return plcNameOffset; }
    public void setPlcNameOffset(int plcNameOffset) { this.plcNameOffset = plcNameOffset; }
    public int getPlcUpdateFlagOffset() { return plcUpdateFlagOffset; }
    public void setPlcUpdateFlagOffset(int plcUpdateFlagOffset) { this.plcUpdateFlagOffset = plcUpdateFlagOffset; }
    public int getPlcSideOffset() { return plcSideOffset; }
    public void setPlcSideOffset(int plcSideOffset) { this.plcSideOffset = plcSideOffset; }
    public int getPlcRowOffset() { return plcRowOffset; }
    public void setPlcRowOffset(int plcRowOffset) { this.plcRowOffset = plcRowOffset; }
    public int getPlcColOffset() { return plcColOffset; }
    public void setPlcColOffset(int plcColOffset) { this.plcColOffset = plcColOffset; }
    public int getPlcYCodeOffset() { return plcYCodeOffset; }
    public void setPlcYCodeOffset(int plcYCodeOffset) { this.plcYCodeOffset = plcYCodeOffset; }

    public int getPlcControlDb() { return plcControlDb; }
    public void setPlcControlDb(int plcControlDb) { this.plcControlDb = plcControlDb; }
    public int getPlcAckOffset() { return plcAckOffset; }
    public void setPlcAckOffset(int plcAckOffset) { this.plcAckOffset = plcAckOffset; }
    public int getPlcStartOffset() { return plcStartOffset; }
    public void setPlcStartOffset(int plcStartOffset) { this.plcStartOffset = plcStartOffset; }
    public int getPlcStopOffset() { return plcStopOffset; }
    public void setPlcStopOffset(int plcStopOffset) { this.plcStopOffset = plcStopOffset; }
    public int getPlcResetOffset() { return plcResetOffset; }
    public void setPlcResetOffset(int plcResetOffset) { this.plcResetOffset = plcResetOffset; }

    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }

    public boolean isAutoFetch() { return autoFetch; }
    public void setAutoFetch(boolean autoFetch) { this.autoFetch = autoFetch; }

    /** 采样间隔中文显示，例如 60000ms -> "60.0秒/次"。 */
    public String getFormattedInterval() {
        return String.format("%.1f", pollIntervalMs / 1000.0) + "秒/次";
    }
}