package com.ltudm.dto;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lớp DTO đại diện cho một phản hồi HTTP Response gửi về cho Client.
 */
public class HttpResponse {
    private int statusCode;
    private String statusText;
    private Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse(int statusCode, String statusText) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        setHeader("Server", "Java-Custom-HTTPServer/1.0");
        setHeader("Connection", "close");
    }

    // Factory methods tiện dụng
    public static HttpResponse okHtml(String html) {
        HttpResponse res = new HttpResponse(200, "OK");
        res.setHeader("Content-Type", "text/html; charset=UTF-8");
        res.setBody(html);
        return res;
    }

    public static HttpResponse okJson(String json) {
        HttpResponse res = new HttpResponse(200, "OK");
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody(json);
        return res;
    }

    public static HttpResponse okText(String text) {
        HttpResponse res = new HttpResponse(200, "OK");
        res.setHeader("Content-Type", "text/plain; charset=UTF-8");
        res.setBody(text);
        return res;
    }

    public static HttpResponse notFound(String message) {
        HttpResponse res = new HttpResponse(404, "Not Found");
        res.setHeader("Content-Type", "text/plain; charset=UTF-8");
        res.setBody(message != null ? message : "404 Not Found");
        return res;
    }

    public static HttpResponse badRequest(String message) {
        HttpResponse res = new HttpResponse(400, "Bad Request");
        res.setHeader("Content-Type", "application/json; charset=UTF-8");
        res.setBody("{\"error\":\"" + (message != null ? message : "Bad Request") + "\"}");
        return res;
    }

    public static HttpResponse fromBytes(int statusCode, String statusText, String contentType, byte[] data) {
        HttpResponse res = new HttpResponse(statusCode, statusText);
        res.setHeader("Content-Type", contentType);
        res.setBodyBytes(data);
        return res;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setBody(String text) {
        if (text != null) {
            this.body = text.getBytes(StandardCharsets.UTF_8);
            setHeader("Content-Length", String.valueOf(this.body.length));
        } else {
            this.body = new byte[0];
            setHeader("Content-Length", "0");
        }
    }

    public void setBodyBytes(byte[] bytes) {
        if (bytes != null) {
            this.body = bytes;
            setHeader("Content-Length", String.valueOf(bytes.length));
        } else {
            this.body = new byte[0];
            setHeader("Content-Length", "0");
        }
    }

    /**
     * Xuất dữ liệu HTTP Response ra luồng OutputStream theo chuẩn giao thức HTTP/1.1.
     */
    public void writeTo(OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        // 1. Status line: HTTP/1.1 200 OK\r\n
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");

        // 2. Headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        // 3. Dòng trống ngăn cách Headers và Body
        sb.append("\r\n");

        // Ghi Headers ra stream
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));

        // 4. Ghi nội dung Body
        if (body != null && body.length > 0) {
            out.write(body);
        }
        out.flush();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
