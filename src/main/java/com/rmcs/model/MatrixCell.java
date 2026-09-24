package com.rmcs.model;

/**
 * 作业矩阵单元格（Y1/Y2 × 列1-2 × 左/右 × 行1-15）的取值状态。
 * 位置定位见 {@link WorkPos}。
 */
public class MatrixCell {

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
}
