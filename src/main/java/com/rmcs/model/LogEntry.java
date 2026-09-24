package com.rmcs.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** 单条动作履历（与原型 LogEntry 对应）。 */
public class LogEntry {

    /** 日志类型：决定日志模块的颜色与样式。 */
    public enum LogType { INFO, PLC, COM, SUCCESS, WARNING, ERROR }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String id;
    private final LogType type;
    private final LocalTime time;
    private final String message;

    public LogEntry(String id, LogType type, String message) {
        this.id = id;
        this.type = type;
        this.time = LocalTime.now();
        this.message = message;
    }

    public String getId() { return id; }
    public LogType getType() { return type; }
    public LocalTime getTime() { return time; }
    public String getFormattedTime() { return time.format(FMT); }
    public String getMessage() { return message; }
}