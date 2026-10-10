package com.ltudm.bus.http;

import com.ltudm.dao.StaticFileDAO;
import com.ltudm.dto.HttpRequest;
import com.ltudm.dto.HttpResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tầng ROUTING: Ánh xạ giữa HTTP Method + Path và bộ xử lý HttpHandler tương ứng.
 * Quy trình chuẩn: REQUEST -> ROUTING -> HANDLER -> RESPONSE.
 */
public class HttpRouter {
    private final Map<String, HttpHandler> routes = new ConcurrentHashMap<>();
    private final StaticFileDAO staticFileDAO;

    public HttpRouter(StaticFileDAO staticFileDAO) {
        this.staticFileDAO = staticFileDAO;
    }

    public void get(String path, HttpHandler handler) {
        addRoute("GET", path, handler);
    }

    public void post(String path, HttpHandler handler) {
        addRoute("POST", path, handler);
    }

    public void addRoute(String method, String path, HttpHandler handler) {
        String key = method.toUpperCase() + ":" + normalizePath(path);
        routes.put(key, handler);
    }

    /**
     * Định tuyến yêu cầu đến Handler phù hợp, hoặc phục vụ tệp tĩnh, hoặc trả về lỗi 404.
     */
    public HttpResponse route(HttpRequest request) {
        String method = request.getMethod();
        String path = normalizePath(request.getPath());
        String routeKey = method + ":" + path;

        // 1. Kiểm tra các route API đã đăng ký
        HttpHandler handler = routes.get(routeKey);
        if (handler != null) {
            try {
                return handler.handle(request);
            } catch (Exception e) {
                return HttpResponse.fromBytes(500, "Internal Server Error", "text/plain; charset=UTF-8",
                        ("500 Internal Server Error: " + e.getMessage()).getBytes());
            }
        }

        // 2. Nếu là GET, kiểm tra xem có tệp tĩnh tương ứng trong www/ hay không (Bài tập mở rộng #3)
        if ("GET".equalsIgnoreCase(method)) {
            String relativeFilePath = path.equals("/") ? "index.html" : path;
            if (staticFileDAO.exists(relativeFilePath)) {
                byte[] data = staticFileDAO.readFileBytes(relativeFilePath);
                if (data != null) {
                    String mime = staticFileDAO.getMimeType(relativeFilePath);
                    return HttpResponse.fromBytes(200, "OK", mime, data);
                }
            }
        }

        // 3. Nếu không tìm thấy route hay file tĩnh nào -> Ca kiểm thử 3: Trả về 404 Not Found
        return HttpResponse.notFound("404 Not Found");
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "/";
        if (!path.startsWith("/")) path = "/" + path;
        // Bỏ dấu gạch chéo thừa ở cuối nếu không phải là "/"
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
