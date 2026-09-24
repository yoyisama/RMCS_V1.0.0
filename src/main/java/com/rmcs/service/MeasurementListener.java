package com.rmcs.service;

import com.rmcs.device.Measurement;

/**
 * 电阻计服务回调。
 * 回调可能来自后台线程，实现方需自行切回 JavaFX UI 线程。
 */
public interface MeasurementListener {

    /** 串口连接成功 */
    void onConnected();

    /** 连接建立失败（仅首次连接阶段） */
    void onConnectionFailed(String reason);

    /** 运行过程中断线（连续读取失败） */
    void onDisconnected(String reason);

    /** 一次已收到仪器响应的测量 */
    void onMeasurement(Measurement measurement);

    /** 单次读取失败（超时等），尚未达到断线阈值 */
    void onReadError(String reason);

    /** 服务层需要记录的业务日志 */
    void onLog(String message);
}
