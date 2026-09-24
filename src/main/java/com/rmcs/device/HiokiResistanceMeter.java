package com.rmcs.device;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HIOKI 电阻计串口实现（基于 jSerialComm）。
 * 指令为 ":MEASure:RESistance?\r\n"，仪器返回科学计数法字符串，例如 "+1.2345E+03"（单位 Ω）。
 */
public class HiokiResistanceMeter implements ResistanceMeter {

    /** 从返回串中提取数值，兼容 +1.2345E+03 / 1234.5 等格式 */
    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("[-+]?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?");

    /** 单次读取的最大字节数，防止异常数据导致内存膨胀 */
    private static final int MAX_LINE_LENGTH = 256;

    private final SerialConfig config;

    private SerialPort port;
    private OutputStream out;
    private InputStream in;

    public HiokiResistanceMeter(SerialConfig config) {
        this.config = config;
    }

    @Override
    public void open() throws Exception {
        SerialPort target = SerialPort.getCommPort(config.getPortName());
        target.setBaudRate(config.getBaudRate());
        target.setNumDataBits(config.getDataBits());
        target.setNumStopBits(config.getStopBits());
        target.setParity(SerialPort.NO_PARITY);
        target.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, config.getReadTimeoutMs(), 0);

        if (!target.openPort()) {
            throw new IOException("无法打开串口 " + config.getPortName()
                    + "，请确认端口未被其它软件占用且线缆已连接");
        }
        this.port = target;
        this.out = target.getOutputStream();
        this.in = target.getInputStream();
    }

    @Override
    public Measurement measure() throws IOException {
        SerialPort current = this.port;
        if (current == null || !current.isOpen()) {
            throw new IOException("串口未打开，无法执行测量");
        }
        out.write(config.getCommand().getBytes(StandardCharsets.US_ASCII));
        out.flush();

        String line = readResponseLine();
        if (line == null) {
            throw new IOException("读取超时：未接收到电阻计响应，请检查波特率或线缆");
        }
        String raw = line.trim();
        Matcher matcher = NUMBER_PATTERN.matcher(raw);
        if (matcher.find()) {
            try {
                return new Measurement(raw, Double.parseDouble(matcher.group()), true);
            } catch (NumberFormatException ignored) {
                // 解析失败时按无效数据处理，交由下方返回
            }
        }
        return new Measurement(raw, Double.NaN, false);
    }

    /**
     * 按行读取返回数据：遇 '\n' 结束、忽略 '\r'，读到超时或流结束则返回已有内容。
     * 采用字节级读取而非 Scanner，避免其内部缓冲导致读到半包数据。
     */
    private String readResponseLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int b = in.read();
            if (b == -1) {
                // 阻塞读超时或流已结束
                return sb.length() == 0 ? null : sb.toString();
            }
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                sb.append((char) b);
            }
            if (sb.length() >= MAX_LINE_LENGTH) {
                break;
            }
        }
        return sb.toString();
    }

    @Override
    public boolean isOpen() {
        return port != null && port.isOpen();
    }

    @Override
    public void close() {
        SerialPort current = this.port;
        this.port = null;
        this.out = null;
        this.in = null;
        if (current != null && current.isOpen()) {
            current.closePort();
        }
    }
}
