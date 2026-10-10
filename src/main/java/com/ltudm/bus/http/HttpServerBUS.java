package com.ltudm.bus.http;

import com.google.gson.Gson;
import com.ltudm.dao.MessageDAO;
import com.ltudm.dao.StaticFileDAO;
import com.ltudm.dao.StudentDAO;
import com.ltudm.dto.HttpRequest;
import com.ltudm.dto.HttpResponse;
import com.ltudm.dto.RequestLog;
import com.ltudm.dto.Student;
import com.ltudm.bus.chat.ChatServerBUS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tầng BUS quản lý vòng đời và điều phối luồng của HTTP Server:
 * - Tiếp nhận kết nối Socket HTTP
 * - Đa luồng: Xử lý nhiều request đồng thời bằng ThreadPool (Bài tập mở rộng #4)
 * - Đăng ký Routing và Handlers cho các ca kiểm thử và bài tập mở rộng
 * - Ghi log chi tiết và gửi dữ liệu về giao diện GUI
 */
public class HttpServerBUS {
    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean isRunning = false;
    private int port = 8080;

    private final StaticFileDAO staticFileDAO = new StaticFileDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final MessageDAO messageDAO = new MessageDAO();
    private final HttpRouter router = new HttpRouter(staticFileDAO);
    private ChatServerBUS chatServerBUS;

    private HttpServerEventListener listener;

    public interface HttpServerEventListener {
        void onServerStarted(int port);
        void onServerStopped();
        void onRequestHandled(RequestLog log);
        void onConsoleLog(String logText);
        void onError(String error);
    }

    public HttpServerBUS() {
        registerDefaultRoutes();
    }

    public void setListener(HttpServerEventListener listener) {
        this.listener = listener;
    }

    public void setChatServerBUS(ChatServerBUS chatServerBUS) {
        this.chatServerBUS = chatServerBUS;
    }

    /**
     * Đăng ký toàn bộ các endpoint kiểm thử và bài tập mở rộng.
     */
    private void registerDefaultRoutes() {
        // CA KIỂM THỬ 1: GET / -> 200 OK • text/html | Body: <h1>HTTP Server</h1>
        router.get("/", request -> HttpResponse.okHtml("<h1>HTTP Server</h1>"));

        // CA KIỂM THỬ 2: GET /api/time -> 200 OK • application/json | Body: {"time":"10:30:00"}
        router.get("/api/time", request -> {
            String currentTime = LocalTime.now().format(TIME_FORMATTER);
            Map<String, String> timeMap = new HashMap<>();
            timeMap.put("time", currentTime);
            return HttpResponse.okJson(GSON.toJson(timeMap));
        });

        // BÀI TẬP MỞ RỘNG 1: POST /api/message -> Đọc dữ liệu từ Request Body
        router.post("/api/message", request -> {
            String bodyContent = request.getBody();
            messageDAO.addMessage(bodyContent, request.getRemoteAddress());

            Map<String, Object> respData = new HashMap<>();
            respData.put("status", "success");
            respData.put("received", bodyContent);
            respData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return HttpResponse.okJson(GSON.toJson(respData));
        });

        // BÀI TẬP MỞ RỘNG 2: GET /api/students -> Trả danh sách sinh viên dạng JSON
        router.get("/api/students", request -> HttpResponse.okJson(studentDAO.toJson()));

        // Mở rộng thêm: POST /api/students -> Thêm sinh viên mới từ Request Body JSON
        router.post("/api/students", request -> {
            try {
                Student s = GSON.fromJson(request.getBody(), Student.class);
                if (s != null && studentDAO.addStudent(s)) {
                    return HttpResponse.okJson("{\"status\":\"success\",\"message\":\"Thêm sinh viên thành công!\"}");
                } else {
                    return HttpResponse.badRequest("Mã sinh viên đã tồn tại hoặc dữ liệu không hợp lệ!");
                }
            } catch (Exception e) {
                return HttpResponse.badRequest("Dữ liệu JSON không đúng cấu trúc!");
            }
        });

        // TÍCH HỢP WEB CHAT VÀ TCP CHAT
        router.get("/chat", request -> {
            if (staticFileDAO.exists("index.html")) {
                byte[] data = staticFileDAO.readFileBytes("index.html");
                if (data != null) {
                    return HttpResponse.fromBytes(200, "OK", "text/html; charset=UTF-8", data);
                }
            }
            return HttpResponse.okHtml("<h1>Web Chat</h1>");
        });

        router.get("/api/chat/messages", request -> {
            if (chatServerBUS == null) return HttpResponse.okJson("[]");
            return HttpResponse.okJson(GSON.toJson(chatServerBUS.getHistoryDAO().getHistory()));
        });

        router.post("/api/chat/send", request -> {
            if (chatServerBUS == null || !chatServerBUS.isRunning()) {
                return HttpResponse.badRequest("TCP Server chưa được bật!");
            }
            try {
                com.ltudm.dto.ChatMessage msg = GSON.fromJson(request.getBody(), com.ltudm.dto.ChatMessage.class);
                if (msg != null && msg.getContent() != null && !msg.getContent().trim().isEmpty()) {
                    msg.setTimestamp(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    chatServerBUS.broadcast(msg);
                    return HttpResponse.okJson("{\"status\":\"success\"}");
                }
            } catch (Exception e) {}
            return HttpResponse.badRequest("Dữ liệu tin nhắn không hợp lệ!");
        });
    }

    /**
     * Khởi động HTTP Server trên cổng port chỉ định.
     */
    public synchronized boolean start(int port) {
        if (isRunning) return true;
        this.port = port;

        try {
            serverSocket = new ServerSocket(port);
            // Bài tập mở rộng #4: Xử lý nhiều request đồng thời bằng ThreadPool
            threadPool = Executors.newFixedThreadPool(30);
            isRunning = true;

            String startMsg = "HTTP Server started.\nListening on http://localhost:" + port + "/\n---------------------------------";
            logConsole(startMsg);

            if (listener != null) {
                listener.onServerStarted(port);
            }

            // Luồng chấp nhận kết nối HTTP liên tục
            threadPool.submit(() -> {
                while (isRunning && !serverSocket.isClosed()) {
                    try {
                        Socket socket = serverSocket.accept();
                        // Chuyển việc xử lý từng HTTP connection cho Worker Thread trong Pool
                        threadPool.submit(() -> handleClientSocket(socket));
                    } catch (IOException e) {
                        if (!isRunning) break;
                    }
                }
            });

            return true;
        } catch (IOException e) {
            String err = "Không thể khởi động HTTP Server trên cổng " + port + ": " + e.getMessage();
            logConsole("[ERROR] " + err);
            if (listener != null) listener.onError(err);
            return false;
        }
    }

    /**
     * Xử lý một yêu cầu Socket từ Client theo quy trình chuẩn:
     * REQUEST -> ROUTING -> HANDLER -> RESPONSE
     */
    private void handleClientSocket(Socket socket) {
        long startTime = System.currentTimeMillis();
        String remoteAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();

        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            // 1. REQUEST: Phân tích luồng byte từ Socket thành HttpRequest
            HttpRequest request = HttpParser.parse(in, remoteAddress);
            if (request == null) {
                return;
            }

            // 2 & 3. ROUTING & HANDLER: Tìm Route và kích hoạt Handler tương ứng
            HttpResponse response = router.route(request);

            // 4. RESPONSE: Xuất kết quả HTTP về cho Client
            response.writeTo(out);

            long duration = System.currentTimeMillis() - startTime;
            int bodyLen = response.getBody() != null ? response.getBody().length : 0;

            // Ghi nhận RequestLog
            RequestLog log = new RequestLog(
                    request.getMethod(),
                    request.getPath(),
                    response.getStatusCode(),
                    response.getStatusText(),
                    bodyLen,
                    remoteAddress,
                    duration
            );

            logConsole(log.toConsoleFormat());

            if (listener != null) {
                listener.onRequestHandled(log);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Dừng HTTP Server.
     */
    public synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }

        logConsole("HTTP Server stopped.");
        if (listener != null) {
            listener.onServerStopped();
        }
    }

    private void logConsole(String text) {
        if (listener != null) {
            listener.onConsoleLog(text);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

    public StudentDAO getStudentDAO() {
        return studentDAO;
    }

    public MessageDAO getMessageDAO() {
        return messageDAO;
    }

    public StaticFileDAO getStaticFileDAO() {
        return staticFileDAO;
    }
}
