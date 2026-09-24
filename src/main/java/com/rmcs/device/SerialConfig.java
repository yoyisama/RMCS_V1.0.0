package com.rmcs.device;

/**
 * 电阻计通讯与测量参数。
 * 按现场 HIOKI 电阻计的实际配置提供默认值，UI 端可通过参数设定窗口实时修改。
 */
public class SerialConfig {

    /** 串口名 */
    private String portName = "COM4";
    /** 波特率 */
    private int baudRate = 9600;
    /** 数据位 */
    private int dataBits = 8;
    /** 停止位 */
    private int stopBits = 1;
    /** 读取超时（毫秒） */
    private int readTimeoutMs = 2000;
    /** 轮询间隔（毫秒）：默认 1 分钟 */
    private long pollIntervalMs = 60_000L;
    /** 测量指令（ASCII） */
    private String command = ":MEASure:RESistance?\r\n";
    /** 标准值（Ω） */
    private double standardValue = 6.532;
    /** 上偏差（Ω） */
    private double upperDev = 0.01;
    /** 下偏差（Ω） */
    private double lowerDev = 0.01;

    public String getPortName() { return portName; }
    public void setPortName(String portName) { this.portName = portName; }

    public int getBaudRate() { return baudRate; }
    public void setBaudRate(int baudRate) { this.baudRate = baudRate; }

    public int getDataBits() { return dataBits; }
    public void setDataBits(int dataBits) { this.dataBits = dataBits; }

    public int getStopBits() { return stopBits; }
    public void setStopBits(int stopBits) { this.stopBits = stopBits; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }

    public long getPollIntervalMs() { return pollIntervalMs; }
    public void setPollIntervalMs(long pollIntervalMs) { this.pollIntervalMs = pollIntervalMs; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public double getStandardValue() { return standardValue; }
    public void setStandardValue(double standardValue) { this.standardValue = standardValue; }

    public double getUpperDev() { return upperDev; }
    public void setUpperDev(double upperDev) { this.upperDev = upperDev; }

    public double getLowerDev() { return lowerDev; }
    public void setLowerDev(double lowerDev) { this.lowerDev = lowerDev; }

    /** 判定下界 = 标准值 - 下偏差 */
    public double getLowerLimit() { return standardValue - lowerDev; }

    /** 判定上界 = 标准值 + 上偏差 */
    public double getUpperLimit() { return standardValue + upperDev; }

    /** 是否落在 [下界, 上界] 的合格区间内 */
    public boolean isInTolerance(double value) {
        return value >= getLowerLimit() && value <= getUpperLimit();
    }
}