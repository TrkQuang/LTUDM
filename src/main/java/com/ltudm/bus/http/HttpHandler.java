package com.ltudm.bus.http;

import com.ltudm.dto.HttpRequest;
import com.ltudm.dto.HttpResponse;

/**
 * Functional Interface xử lý yêu cầu HTTP và trả về kết quả phản hồi.
 * Tuân theo quy trình chuẩn: REQUEST -> ROUTING -> HANDLER -> RESPONSE.
 */
@FunctionalInterface
public interface HttpHandler {
    HttpResponse handle(HttpRequest request) throws Exception;
}
