package com.rmcs.model;

/** 测定标准：标准值与上下偏差（Ω）。 */
public class TestStandard {

    private double standardValue = 6.532;
    private double upperDev = 0.01;
    private double lowerDev = 0.01;

    public TestStandard() {}

    public TestStandard(double standardValue, double upperDev, double lowerDev) {
        this.standardValue = standardValue;
        this.upperDev = upperDev;
        this.lowerDev = lowerDev;
    }

    public double getStandardValue() { return standardValue; }
    public void setStandardValue(double standardValue) { this.standardValue = standardValue; }

    public double getUpperDev() { return upperDev; }
    public void setUpperDev(double upperDev) { this.upperDev = upperDev; }

    public double getLowerDev() { return lowerDev; }
    public void setLowerDev(double lowerDev) { this.lowerDev = lowerDev; }

    public double getLowerLimit() { return standardValue - lowerDev; }
    public double getUpperLimit() { return standardValue + upperDev; }

    public boolean isInTolerance(double value) {
        return value >= getLowerLimit() && value <= getUpperLimit();
    }
}