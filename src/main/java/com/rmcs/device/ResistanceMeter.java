package com.rmcs.device;

/**
 * 电阻计抽象：屏蔽具体通讯实现，便于后续接入其它品牌 / 型号。
 */
public interface ResistanceMeter {

    /** 打开通讯端口，失败时抛出异常并携带可读的失败原因 */
    void open() throws Exception;

    /** 关闭通讯端口并释放资源，可重复调用 */
    void close();

    /** 端口是否处于打开状态 */
    boolean isOpen();

    /** 执行一次测量并返回结果 */
    Measurement measure() throws Exception;
}
