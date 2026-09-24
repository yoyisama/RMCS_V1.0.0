package com.rmcs.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 单条检测履历（与原型 InspectionRecord 对应）。
 * 结构与作业矩阵一致：Y1/Y2 侧 + 第一列/第二列 + 左/右 + 行 1..15。
 */
public class InspectionRecord {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");

    private final String id;
    private final String side;    // Y1 / Y2
    private final int col;        // 1 / 2
    private final String lr;      // 左 / 右
    private final int row;        // 1..15
    private final String productName; // 当前产品名称（来自 PLC DB36 产品名）
    private final double measuredValue;
    private final double standardValue;
    private final boolean result;
    private final LocalDateTime time;

    public InspectionRecord(String id, String side, int col, String lr, int row, String productName,
                            double measuredValue, double standardValue, boolean result) {
        this.id = id;
        this.side = side;
        this.col = col;
        this.lr = lr;
        this.row = row;
        this.productName = productName == null ? "" : productName;
        this.measuredValue = measuredValue;
        this.standardValue = standardValue;
        this.result = result;
        this.time = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getSide() { return side; }
    public int getCol() { return col; }
    public String getLr() { return lr; }
    public int getRow() { return row; }
    public String getProductName() { return productName; }
    public double getMeasuredValue() { return measuredValue; }
    public double getStandardValue() { return standardValue; }
    public boolean isResult() { return result; }
    public LocalDateTime getTime() { return time; }

    public String getFormattedTime() { return time.format(FMT); }

    /** 位置描述（不含行），形如 "Y1第一列左侧"。 */
    public String getPosition() {
        return side + "第" + (col == 1 ? "一" : "二") + "列" + lr + "侧";
    }

    /** 履历事件行，形如 "14点05分30秒：Y1第一列左侧第一行"。 */
    public String getEventText() {
        return String.format("%d点%d分%d秒：%s",
                time.getHour(), time.getMinute(), time.getSecond(), describe());
    }

    /** 完整描述，形如 "Y1第一列左侧第一行"。 */
    public String describe() {
        return getPosition() + WorkPos.rowName(row);
    }

    /** 标准格式 0.000，与原型的 value.toFixed(3) 对应。 */
    public String getFormattedMeasured() { return formatValue(measuredValue); }
    public String getFormattedStandard() { return formatValue(standardValue); }

    public static String formatValue(double v) {
        return String.format("%.3f", v);
    }
}
