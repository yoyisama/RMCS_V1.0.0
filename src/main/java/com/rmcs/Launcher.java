package com.rmcs;

import javafx.application.Application;

/**
 * 打包专用启动类。
 * 打包成 exe（非模块化）时，主类不能直接继承 Application，
 * 否则 Java 启动器会因缺少 JavaFX 运行时组件而报错。
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
