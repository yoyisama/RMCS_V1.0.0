package com.rmcs.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 单条检测履历（与原型 InspectionRecord 对应）。 */
public class InspectionRecord {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");

    private final String id;
    private final int row;
    private final String position;
    private final double measuredValue;
    private final double standardValue;
    private final boolean result;
    private final LocalDateTime time;

    public InspectionRecord(String id, int row, String position,
                            double measuredValue, double standardValue, boolean result) {
        this.id = id;
        this.row = row;
        this.position = position;
        this.measuredValue = measuredValue;
        this.standardValue = standardValue;
        this.result = result;
        this.time = LocalDateTime.now();
    }

    public String getId() { return id; }
    public int getRow() { return row; }
    public String getPosition() { return position; }
    public double getMeasuredValue() { return measuredValue; }
    public double getStandardValue() { return standardValue; }
    public boolean isResult() { return result; }
    public LocalDateTime getTime() { return time; }

    public String getFormattedTime() { return time.format(FMT); }

    /** 标准格式 0.000，与原型的 value.toFixed(3).replace('.', ',') 对应。 */
    public String getFormattedMeasured() { return formatValue(measuredValue); }
    public String getFormattedStandard() { return formatValue(standardValue); }

    public static String formatValue(double v) {
        return String.format("%.3f", v);
    }
}