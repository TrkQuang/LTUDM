package com.ltudm.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Lớp DTO đại diện cho một yêu cầu HTTP Request nhận từ Client.
 */
public class HttpRequest {
    private String method;              // GET, POST, etc.
    private String path;                // Đường dẫn (ví dụ: /, /api/time, /api/message)
    private String protocol = "HTTP/1.1";
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> queryParams = new HashMap<>();
    private String body = "";
    private byte[] rawBody = new byte[0];
    private String remoteAddress = "";  // Ví dụ: 127.0.0.1:55321

    public HttpRequest() {}

    public String getHeader(String name) {
        if (name == null) return null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int getContentLength() {
        String val = getHeader("Content-Length");
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getContentType() {
        return getHeader("Content-Type");
    }

    // Getters and Setters
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public void addQueryParam(String key, String value) {
        this.queryParams.put(key, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body != null ? body : "";
    }

    public byte[] getRawBody() {
        return rawBody;
    }

    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody != null ? rawBody : new byte[0];
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
}
