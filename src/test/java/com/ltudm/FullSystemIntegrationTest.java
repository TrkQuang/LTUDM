package com.ltudm;

import com.ltudm.bus.chat.ChatClientBUS;
import com.ltudm.bus.chat.ChatServerBUS;
import com.ltudm.bus.http.HttpServerBUS;
import com.ltudm.dto.ChatMessage;
import com.ltudm.dto.MessageType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Kiểm thử tích hợp tự động cho toàn bộ yêu cầu của BÀI 11 và BÀI 12:
 * - Bài 11: TCP Chat Multi-Client (Connect, Broadcast, Private Message, Online List, Disconnect)
 * - Bài 12: HTTP Server (Ca 1 GET /, Ca 2 GET /api/time, Ca 3 GET /abc 404, Mở rộng 1 POST, Mở rộng 2 Student JSON, Mở rộng 3 Static CSS, Mở rộng 4 Multi-thread)
 */
public class FullSystemIntegrationTest {
    private static ChatServerBUS chatServer;
    private static HttpServerBUS httpServer;

    private static final int CHAT_PORT = 9988;
    private static final int HTTP_PORT = 8089;

    @BeforeAll
    public static void setup() throws Exception {
        // Khởi động TCP Server
        chatServer = new ChatServerBUS();
        assertTrue(chatServer.startServer(CHAT_PORT), "TCP Server phải khởi động thành công");

        // Khởi động HTTP Server
        httpServer = new HttpServerBUS();
        assertTrue(httpServer.start(HTTP_PORT), "HTTP Server phải khởi động thành công");

        Thread.sleep(300);
    }

    @AfterAll
    public static void teardown() {
        if (chatServer != null) chatServer.stopServer();
        if (httpServer != null) httpServer.stop();
    }

    // ==========================================
    // CÁC TEST CASE BÀI 11: TCP CHAT NHIỀU CLIENT
    // ==========================================

    @Test
    public void testTcpChatFlow() throws Exception {
        ChatClientBUS client1 = new ChatClientBUS();
        ChatClientBUS client2 = new ChatClientBUS();

        List<ChatMessage> client2ReceivedMessages = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch msgLatch = new CountDownLatch(1);
        CountDownLatch privateLatch = new CountDownLatch(1);

        client2.setListener(new ChatClientBUS.ClientEventListener() {
            @Override
            public void onConnected(String myUsername, String host, int port) {}
            @Override
            public void onDisconnected() {}
            @Override
            public void onMessageReceived(ChatMessage message) {
                client2ReceivedMessages.add(message);
                if (message.getType() == MessageType.PUBLIC && "Xin chào từ Client 1".equals(message.getContent())) {
                    msgLatch.countDown();
                }
                if (message.getType() == MessageType.PRIVATE && "Tin nhắn bí mật cho Bob".equals(message.getContent())) {
                    privateLatch.countDown();
                }
            }
            @Override
            public void onUserListUpdated(List<String> users) {}
            @Override
            public void onError(String errorMessage) {}
        });

        // 1. Kết nối 2 client với username
        assertTrue(client1.connect("localhost", CHAT_PORT, "Alice"), "Alice kết nối thành công");
        assertTrue(client2.connect("localhost", CHAT_PORT, "Bob"), "Bob kết nối thành công");

        assertEquals("Alice", client1.getCurrentUsername());
        assertEquals("Bob", client2.getCurrentUsername());

        // 2. Client 1 gửi tin nhắn Broadcast -> Client 2 nhận được
        client1.sendPublicMessage("Xin chào từ Client 1");
        assertTrue(msgLatch.await(3, TimeUnit.SECONDS), "Client 2 phải nhận được tin nhắn Broadcast của Alice");

        // 3. Client 1 gửi tin nhắn riêng cho Bob -> Bob nhận được
        client1.sendPrivateMessage("Bob", "Tin nhắn bí mật cho Bob");
        assertTrue(privateLatch.await(3, TimeUnit.SECONDS), "Bob phải nhận được tin nhắn riêng từ Alice");

        // 4. Ngắt kết nối an toàn không làm lỗi server
        client1.disconnect();
        client2.disconnect();
        assertFalse(client1.isConnected());
        assertFalse(client2.isConnected());
    }

    // ==========================================
    // CÁC TEST CASE BÀI 12: HTTP VÀ HTTP SERVER
    // ==========================================

    @Test
    public void testCase1_GetRoot() throws Exception {
        // CA KIỂM THỬ 1: GET / -> 200 OK • text/html | Body: <h1>HTTP Server</h1>
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + HTTP_PORT + "/").openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        assertEquals(200, conn.getResponseCode(), "Mã phản hồi phải là 200 OK");
        assertTrue(conn.getContentType().contains("text/html"), "Content-Type phải chứa text/html");

        String body = readStream(conn.getInputStream());
        assertTrue(body.contains("<h1>HTTP Server</h1>"), "Body phải chứa <h1>HTTP Server</h1>");
    }

    @Test
    public void testCase2_GetTime() throws Exception {
        // CA KIỂM THỬ 2: GET /api/time -> 200 OK • application/json | Body: {"time":"..."}
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + HTTP_PORT + "/api/time").openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        assertEquals(200, conn.getResponseCode(), "Mã phản hồi phải là 200 OK");
        assertTrue(conn.getContentType().contains("application/json"), "Content-Type phải chứa application/json");

        String body = readStream(conn.getInputStream());
        assertTrue(body.contains("\"time\""), "Body JSON phải chứa trường 'time'");
    }

    @Test
    public void testCase3_GetNotFound() throws Exception {
        // CA KIỂM THỬ 3: GET /abc -> 404 Not Found • text/plain | Body: 404 Not Found
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + HTTP_PORT + "/abc").openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        assertEquals(404, conn.getResponseCode(), "Mã phản hồi phải là 404 Not Found");
        assertTrue(conn.getContentType().contains("text/plain"), "Content-Type phải chứa text/plain");

        String body = readStream(conn.getErrorStream());
        assertTrue(body.contains("404 Not Found"), "Body phải là 404 Not Found");
    }

    @Test
    public void testExtended1_PostMessage() throws Exception {
        // BÀI TẬP MỞ RỘNG 1: POST /api/message -> Đọc dữ liệu từ Request Body
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + HTTP_PORT + "/api/message").openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String jsonBody = "{\"sender\":\"Tester\",\"content\":\"Dữ liệu kiểm thử Body\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(200, conn.getResponseCode());
        String resp = readStream(conn.getInputStream());
        assertTrue(resp.contains("\"status\":\"success\""), "Phản hồi phải xác nhận status: success");
        assertTrue(resp.contains("Dữ liệu kiểm thử Body"), "Phản hồi phải chứa dữ liệu body đã gửi lên");
    }

    @Test
    public void testExtended2_GetStudents() throws Exception {
        // BÀI TẬP MỞ RỘNG 2: GET /api/students -> Trả danh sách sinh viên dạng JSON
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + HTTP_PORT + "/api/students").openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        assertEquals(200, conn.getResponseCode());
        assertTrue(conn.getContentType().contains("application/json"));

        String body = readStream(conn.getInputStream());
        assertTrue(body.contains("Nguyễn Văn An"), "Phải chứa sinh viên mẫu Nguyễn Văn An");
        assertTrue(body.contains("SV001"), "Phải chứa mã SV001");
    }

    @Test
    public void testExtended3_StaticFiles() throws Exception {
        // BÀI TẬP MỞ RỘNG 3: Phục vụ tệp HTML và CSS
        HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:" + HTTP_PORT + "/style.css").openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        assertEquals(200, conn.getResponseCode(), "Phải trả về 200 OK khi đọc style.css");
        assertTrue(conn.getContentType().contains("text/css"), "Content-Type phải là text/css");
    }

    @Test
    public void testExtended4_ConcurrentRequests() throws Exception {
        // BÀI TẬP MỞ RỘNG 4: Xử lý nhiều Request đồng thời bằng ThreadPool
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Integer> statusCodes = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    HttpURLConnection c = (HttpURLConnection) new URL("http://localhost:" + HTTP_PORT + "/api/time").openConnection();
                    c.setRequestMethod("GET");
                    statusCodes.add(c.getResponseCode());
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Tất cả các thread đồng thời phải hoàn tất kịp thời");
        assertEquals(threadCount, statusCodes.size());
        for (int code : statusCodes) {
            assertEquals(200, code);
        }
    }

    private String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}
