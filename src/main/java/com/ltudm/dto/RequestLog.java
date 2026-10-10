package com.ltudm.dto;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp DTO lưu nhật ký một yêu cầu HTTP được Server tiếp nhận và xử lý.
 */
public class RequestLog {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String timestamp;
    private String method;
    private String path;
    private int statusCode;
    private String statusText;
    private int bytesLength;
    private String remoteAddress;
    private long durationMs;

    public RequestLog(String method, String path, int statusCode, String statusText, int bytesLength, String remoteAddress, long durationMs) {
        this.timestamp = LocalTime.now().format(TIME_FORMATTER);
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.bytesLength = bytesLength;
        this.remoteAddress = remoteAddress;
        this.durationMs = durationMs;
    }

    /**
     * Định dạng Console log chuẩn như trong ảnh đề bài Bài 12:
     * [GET] /
     * 200 OK 12 bytes
     * Remote: 127.0.0.1:55321
     */
    public String toConsoleFormat() {
        return "[" + method + "] " + path + "\n"
                + statusCode + " " + statusText + "   " + bytesLength + " bytes\n"
                + "Remote: " + remoteAddress + "\n"
                + "---------------------------------";
    }

    // Getters
    public String getTimestamp() {
        return timestamp;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public int getBytesLength() {
        return bytesLength;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
