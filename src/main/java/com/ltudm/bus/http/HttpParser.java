package com.ltudm.bus.http;

import com.ltudm.dto.HttpRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Phân tích cú pháp dòng byte nhận từ Socket InputStream thành đối tượng HttpRequest.
 */
public class HttpParser {

    public static HttpRequest parse(InputStream in, String remoteAddress) throws IOException {
        HttpRequest request = new HttpRequest();
        request.setRemoteAddress(remoteAddress);

        // 1. Đọc Request-Line (Ví dụ: GET /api/time HTTP/1.1)
        String requestLine = readLine(in);
        if (requestLine == null || requestLine.trim().isEmpty()) {
            return null;
        }

        String[] parts = requestLine.trim().split("\\s+");
        if (parts.length < 2) {
            return null;
        }

        request.setMethod(parts[0].toUpperCase());
        String fullPath = parts[1];
        if (parts.length >= 3) {
            request.setProtocol(parts[2]);
        }

        // Tách query parameters nếu có (ví dụ: /api/students?id=SV001)
        int questionIndex = fullPath.indexOf('?');
        if (questionIndex >= 0) {
            request.setPath(fullPath.substring(0, questionIndex));
            String queryStr = fullPath.substring(questionIndex + 1);
            for (String param : queryStr.split("&")) {
                int eq = param.indexOf('=');
                if (eq > 0) {
                    request.addQueryParam(param.substring(0, eq), param.substring(eq + 1));
                } else if (!param.isEmpty()) {
                    request.addQueryParam(param, "");
                }
            }
        } else {
            request.setPath(fullPath);
        }

        // 2. Đọc các Headers cho đến khi gặp dòng trống (\r\n)
        String headerLine;
        while ((headerLine = readLine(in)) != null && !headerLine.trim().isEmpty()) {
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                String key = headerLine.substring(0, colonIndex).trim();
                String value = headerLine.substring(colonIndex + 1).trim();
                request.addHeader(key, value);
            }
        }

        // 3. Đọc dữ liệu Request Body (Bài 12 - Bài tập mở rộng #1: Đọc dữ liệu từ Request Body)
        int contentLength = request.getContentLength();
        if (contentLength > 0) {
            byte[] bodyBytes = new byte[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                int read = in.read(bodyBytes, totalRead, contentLength - totalRead);
                if (read == -1) break;
                totalRead += read;
            }
            request.setRawBody(bodyBytes);
            request.setBody(new String(bodyBytes, StandardCharsets.UTF_8));
        }

        return request;
    }

    /**
     * Đọc 1 dòng từ InputStream cho đến khi gặp ký tự \n.
     */
    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\n') {
                break;
            }
            if (b != '\r') {
                baos.write(b);
            }
        }
        if (b == -1 && baos.size() == 0) {
            return null;
        }
        return baos.toString(StandardCharsets.UTF_8);
    }
}
