package com.rmcs.plc;

/** PLC 服务回调（可能来自后台线程，实现方需自行切回 UI 线程）。 */
public interface PlcListener {

    /** 连接建立成功 */
    void onConnected();

    /** 连接建立失败（仅首次连接阶段） */
    void onConnectionFailed(String reason);

    /** 运行过程中断线（连续读取失败） */
    void onDisconnected(String reason);

    /** PLC 数据更新标志变化（新测点就绪），携带最新解析数据 */
    void onData(PlcData data);

    /** 连接过程状态提示（端口探测 / 握手进度等），默认忽略 */
    default void onStatus(String msg) { }
}
