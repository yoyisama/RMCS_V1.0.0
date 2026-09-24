package com.rmcs.model;

/** 主题模式（深色 / 浅色）。 */
public enum ThemeMode {
    DARK, LIGHT;

    public ThemeMode toggle() {
        return this == DARK ? LIGHT : DARK;
    }
}