package com.ltudm.dao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Tầng DAO đọc và phục vụ tệp tĩnh (HTML, CSS, JS, hình ảnh) từ thư mục www/
 * cho Bài 12 - Bài tập mở rộng #3.
 */
public class StaticFileDAO {
    private final File baseDir;
    private final Map<String, String> mimeTypes = new HashMap<>();

    public StaticFileDAO() {
        this("www");
    }

    public StaticFileDAO(String basePath) {
        this.baseDir = new File(basePath);
        if (!this.baseDir.exists()) {
            this.baseDir.mkdirs();
        }

        // Đăng ký bảng tra cứu MIME types
        mimeTypes.put("html", "text/html; charset=UTF-8");
        mimeTypes.put("htm", "text/html; charset=UTF-8");
        mimeTypes.put("css", "text/css; charset=UTF-8");
        mimeTypes.put("js", "application/javascript; charset=UTF-8");
        mimeTypes.put("json", "application/json; charset=UTF-8");
        mimeTypes.put("txt", "text/plain; charset=UTF-8");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("gif", "image/gif");
        mimeTypes.put("svg", "image/svg+xml");
        mimeTypes.put("ico", "image/x-icon");
    }

    /**
     * Kiểm tra xem tệp tĩnh có tồn tại trong thư mục www/ hay không.
     */
    public boolean exists(String relativePath) {
        File file = resolveSafeFile(relativePath);
        return file != null && file.exists() && file.isFile();
    }

    /**
     * Đọc toàn bộ nội dung tệp thành mảng byte.
     */
    public byte[] readFileBytes(String relativePath) {
        File file = resolveSafeFile(relativePath);
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Xác định Content-Type tương ứng theo phần mở rộng tệp.
     */
    public String getMimeType(String relativePath) {
        int dot = relativePath.lastIndexOf('.');
        if (dot >= 0 && dot < relativePath.length() - 1) {
            String ext = relativePath.substring(dot + 1).toLowerCase();
            String mime = mimeTypes.get(ext);
            if (mime != null) return mime;
        }
        return "application/octet-stream";
    }

    /**
     * Giải quyết đường dẫn và ngăn chặn lỗ hổng Path Traversal (../)
     */
    private File resolveSafeFile(String relativePath) {
        if (relativePath == null) return null;
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        if (relativePath.isEmpty()) {
            relativePath = "index.html";
        }

        try {
            File target = new File(baseDir, relativePath).getCanonicalFile();
            File root = baseDir.getCanonicalFile();
            if (!target.getPath().startsWith(root.getPath())) {
                // Phát hiện cố gắng truy cập ra ngoài thư mục www/
                return null;
            }
            return target;
        } catch (IOException e) {
            return null;
        }
    }
}
