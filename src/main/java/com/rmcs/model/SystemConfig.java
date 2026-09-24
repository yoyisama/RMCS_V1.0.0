package com.rmcs.model;

/** 系统配置（UI 层）：串口/PLC IP/采样间隔/自动开关等。 */
public class SystemConfig {

    private TestStandard standard = new TestStandard();
    private String comPort = "COM4";
    private int baudRate = 9600;
    private String plcIp = "192.168.1.10";
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

    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }

    public boolean isAutoFetch() { return autoFetch; }
    public void setAutoFetch(boolean autoFetch) { this.autoFetch = autoFetch; }

    /** 采样间隔中文显示，例如 60000ms -> "60.0秒/次"。 */
    public String getFormattedInterval() {
        return String.format("%.1f", pollIntervalMs / 1000.0) + "秒/次";
    }
}