package com.rmcs.service;

import com.rmcs.device.Measurement;
import com.rmcs.device.ResistanceMeter;
import com.rmcs.device.SerialConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 电阻计后台服务。
 * 负责建立连接、连接后立即查询一次，随后按配置间隔定时轮询。
 * 所有串口 I/O 都在名为 rmcs-meter 的后台单线程中执行，保证同一时刻只有一个串口事务。
 */
public class MeterService {

    /** 连续读取失败多少次判定为断线 */
    private static final int MAX_FAIL_BEFORE_OFFLINE = 3;

    private final ResistanceMeter meter;
    private final SerialConfig config;
    private final MeasurementListener listener;
    private final ScheduledExecutorService scheduler;

    private volatile boolean connected;
    private volatile boolean polling;
    private volatile ScheduledFuture<?> pollTask;
    private int failCount;

    public MeterService(ResistanceMeter meter, SerialConfig config, MeasurementListener listener) {
        this.meter = meter;
        this.config = config;
        this.listener = listener;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "rmcs-meter");
            thread.setDaemon(true);
            return thread;
        });
    }

    /** 建立连接；成功后立即查询一次，再转入定时轮询。 */
    public void connect() {
        try {
            scheduler.execute(this::connectInternal);
        } catch (RejectedExecutionException e) {
            // 服务已 shutdown（例如重连串口后旧服务关闭），不应抛出到调用方
            listener.onConnectionFailed("采集服务已关闭，无法建立连接");
        }
    }

    private void connectInternal() {
        try {
            meter.open();
            connected = true;
            failCount = 0;
            listener.onConnected();
            pollOnce();       // 连接成功后先查询一次
            startPolling();   // 之后按间隔定时轮询
        } catch (Exception e) {
            connected = false;
            listener.onConnectionFailed(describe(e));
        }
    }

    /** 开始 / 恢复定时轮询（未连接时不生效，已在轮询时不重复启动）。 */
    public void startPolling() {
        if (!connected || polling) {
            return;
        }
        polling = true;
        long interval = config.getPollIntervalMs();
        pollTask = scheduler.scheduleWithFixedDelay(this::pollOnce, interval, interval, TimeUnit.MILLISECONDS);
        listener.onLog("电阻计定时轮询已启动，间隔 " + (interval / 1000) + " 秒");
    }

    /** 暂停定时轮询（串口保持打开）。 */
    public void stopPolling() {
        if (!polling) {
            return;
        }
        polling = false;
        ScheduledFuture<?> task = this.pollTask;
        this.pollTask = null;
        if (task != null) {
            task.cancel(false);
        }
        listener.onLog("电阻计定时轮询已暂停");
    }

    /** 关闭串口并释放后台线程，退出系统前调用。 */
    public void shutdown() {
        polling = false;
        ScheduledFuture<?> task = this.pollTask;
        this.pollTask = null;
        if (task != null) {
            task.cancel(false);
        }
        try {
            scheduler.submit(meter::close);
        } catch (RejectedExecutionException ignored) {
            meter.close();
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public SerialConfig getConfig() {
        return config;
    }

    /** 执行一次测量，并把结果或异常回传给监听方。 */
    private void pollOnce() {
        if (!connected) {
            return;
        }
        try {
            Measurement measurement = meter.measure();
            failCount = 0;
            listener.onMeasurement(measurement);
        } catch (Exception e) {
            failCount++;
            String reason = describe(e);
            listener.onReadError(reason);
            if (failCount >= MAX_FAIL_BEFORE_OFFLINE) {
                connected = false;
                stopPolling();
                listener.onDisconnected("连续 " + failCount + " 次读取失败：" + reason);
            }
        }
    }

    private String describe(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message;
    }
}
