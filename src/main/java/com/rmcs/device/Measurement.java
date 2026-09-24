package com.rmcs.device;

/** 单次测量结果：电阻计原始返回串 + 解析后的阻值。 */
public class Measurement {

    private final String raw;
    private final double value;
    private final boolean valid;

    public Measurement(String raw, double value, boolean valid) {
        this.raw = raw;
        this.value = value;
        this.valid = valid;
    }

    /** 电阻计原始返回串（已去除首尾空白），用于日志留痕 */
    public String getRaw() {
        return raw;
    }

    /** 解析后的阻值，单位 Ω；valid 为 false 时无意义 */
    public double getValue() {
        return value;
    }

    /** 是否成功解析出数值（false 通常表示超量程 OVER 或非数值返回） */
    public boolean isValid() {
        return valid;
    }
}
