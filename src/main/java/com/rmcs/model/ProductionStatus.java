package com.rmcs.model;

/** 生产运行遥测：完成数量 / 设备状态 / 运行时长。 */
public class ProductionStatus {

    /** 设备状态：运行中 / 待机 / 已停止 / 复位中。 */
    public enum MachineStatus { RUNNING, STANDYBY, STOPPED, RESETTING }

    private long completedCount = 8_921L;
    private MachineStatus status = MachineStatus.STANDYBY;
    private long runDurationSeconds = 0L;

    public ProductionStatus() {}

    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }

    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }

    public long getRunDurationSeconds() { return runDurationSeconds; }
    public void setRunDurationSeconds(long runDurationSeconds) { this.runDurationSeconds = runDurationSeconds; }

    public void incrementCompleted() { completedCount++; }
    public void incrementDuration() { runDurationSeconds++; }

    public void resetCounters() {
        completedCount = 0L;
        runDurationSeconds = 0L;
    }

    /** 时长格式 hh:mm:ss。 */
    public String getFormattedDuration() {
        long h = runDurationSeconds / 3600;
        long m = (runDurationSeconds % 3600) / 60;
        long s = runDurationSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /** 中文状态显示。 */
    public String getStatusLabel() {
        switch (status) {
            case RUNNING: return "运行中";
            case STANDYBY: return "待机";
            case STOPPED: return "已停止";
            case RESETTING: return "复位中";
            default: return status.name();
        }
    }
}