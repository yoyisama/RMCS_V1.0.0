package com.rmcs.model;

/**
 * 7 行 × 3 区矩阵的单个单元格：行号（1..7）+ 位置（L/M/R）。
 * 与原型 MatrixState 的内嵌对象对应。
 */
public class MatrixCell {

    /** 矩阵列：左区 / 中区 / 右区。 */
    public enum Position {
        L("左区 (L)"),
        M("中区 (M)"),
        R("右区 (R)");

        private final String label;

        Position(String label) { this.label = label; }

        public String getLabel() { return label; }
    }

    private Double value;
    private Boolean result;
    private boolean testing;

    public MatrixCell() {}

    public MatrixCell(Double value, Boolean result, boolean testing) {
        this.value = value;
        this.result = result;
        this.testing = testing;
    }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public Boolean getResult() { return result; }
    public void setResult(Boolean result) { this.result = result; }

    public boolean isTesting() { return testing; }
    public void setTesting(boolean testing) { this.testing = testing; }

    /** 组合单元格的矩阵键，形如 "2-M"。 */
    public static String keyOf(int row, Position pos) {
        return row + "-" + pos.name();
    }
}