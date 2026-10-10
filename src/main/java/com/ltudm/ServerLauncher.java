package com.ltudm;

import com.ltudm.bus.chat.ChatServerBUS;
import com.ltudm.bus.http.HttpServerBUS;
import com.ltudm.dto.RequestLog;
import com.ltudm.dto.User;

import java.util.List;

/**
 * Khởi chạy độc lập Server (TCP Chat + HTTP Web Server) ở chế độ Console/Headless.
 * Dành cho triển khai Cloud (Railway, Render, VPS, Docker) hoặc chạy ngầm không cần GUI.
 */
public class ServerLauncher {
    public static void main(String[] args) {
        int tcpPort = 8888;
        int httpPort = 8080;

        // Đọc cổng từ biến môi trường (PaaS như Railway/Render/Heroku/Fly.io)
        String envPort = System.getenv("PORT");
        String envTcpPort = System.getenv("PORT_TCP");
        String envHttpPort = System.getenv("PORT_HTTP");

        if (envTcpPort != null) {
            try { tcpPort = Integer.parseInt(envTcpPort); } catch (Exception ignored) {}
        }
        if (envHttpPort != null) {
            try { httpPort = Integer.parseInt(envHttpPort); } catch (Exception ignored) {}
        } else if (envPort != null) {
            try { httpPort = Integer.parseInt(envPort); } catch (Exception ignored) {}
        }

        System.out.println("=========================================================");
        System.out.println("  LTUDM SERVER KHỞI CHẠY (HEADLESS / CLOUD CONSOLE MODE) ");
        System.out.println("=========================================================");

        ChatServerBUS chatServer = new ChatServerBUS();
        chatServer.setListener(new ChatServerBUS.ServerEventListener() {
            @Override
            public void onServerStarted(int port) {
                System.out.println("[TCP SERVER]  Listening trên cổng TCP: " + port);
            }

            @Override
            public void onServerStopped() {
                System.out.println("[TCP SERVER]  Đã dừng.");
            }

            @Override
            public void onLog(String logMessage) {
                System.out.println("[TCP LOG]     " + logMessage);
            }

            @Override
            public void onClientListChanged(List<User> activeUsers) {
                System.out.println("[TCP ONLINE]  " + activeUsers.size() + " client đang kết nối");
            }

            @Override
            public void onError(String errorMessage) {
                System.err.println("[TCP ERROR]   " + errorMessage);
            }
        });

        HttpServerBUS httpServer = new HttpServerBUS();
        httpServer.setChatServerBUS(chatServer);
        httpServer.setListener(new HttpServerBUS.HttpServerEventListener() {
            @Override
            public void onServerStarted(int port) {
                System.out.println("[HTTP SERVER] Listening trên cổng HTTP: " + port);
                System.out.println("[HTTP SERVER] Mở trình duyệt: http://localhost:" + port + "/");
                System.out.println("[HTTP SERVER] Giao diện Web Chat: http://localhost:" + port + "/index.html hoặc /chat");
            }

            @Override
            public void onServerStopped() {
                System.out.println("[HTTP SERVER] Đã dừng.");
            }

            @Override
            public void onRequestHandled(RequestLog log) {
                System.out.println("[HTTP REQ]    " + log.getMethod() + " " + log.getPath() + " -> " + log.getStatusCode() + " (" + log.getDurationMs() + "ms)");
            }

            @Override
            public void onConsoleLog(String logText) {
                System.out.println("[HTTP LOG]    " + logText);
            }

            @Override
            public void onError(String error) {
                System.err.println("[HTTP ERROR]  " + error);
            }
        });

        // Khởi động cả 2 server
        boolean tcpOk = chatServer.startServer(tcpPort);
        boolean httpOk = httpServer.start(httpPort);

        if (!tcpOk || !httpOk) {
            System.err.println("[LỖI] Không thể khởi động đầy đủ dịch vụ!");
        } else {
            System.out.println("=========================================================");
            System.out.println("  SẴN SÀNG KẾT NỐI: CẢ JAVA CLIENT VÀ WEB CLIENT!       ");
            System.out.println("=========================================================");
        }

        // Tự động giải phóng tài nguyên khi tắt (Ctrl+C hoặc Docker stop)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[HỆ THỐNG] Đang dừng server...");
            chatServer.stopServer();
            httpServer.stop();
            System.out.println("[HỆ THỐNG] Đã tắt toàn bộ dịch vụ an toàn.");
        }));

        // Giữ tiến trình sống
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {}
    }
}
