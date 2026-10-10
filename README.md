# HỆ THỐNG LẬP TRÌNH ỨNG DỤNG MẠNG (LTUDM)
## HOÀN THÀNH BÀI 11 (TCP CHAT MULTI-CLIENT) & BÀI 12 (HTTP SERVER)
### Kiến trúc chuẩn 3 lớp: GUI - BUS - DAO | Ngôn ngữ: Java 17 | Giao diện: FlatLaf Modern UI

---

## 🌟 1. GIỚI THIỆU TỔNG QUAN

Dự án này tích hợp trọn vẹn cả **Bài 11 (TCP Chat nhiều Client)** và **Bài 12 (HTTP và HTTP Server)** vào trong **MỘT ứng dụng duy nhất**, tuân thủ nghiêm ngặt mô hình kiến trúc phân tầng chuyên nghiệp:

- **GUI (Graphical User Interface)**: Giao diện trực quan, hiện đại thiết kế theo phong cách Flat Design (FlatLaf), màu sắc chuẩn UI/UX, hỗ trợ đầy đủ các thao tác kiểm thử trực quan.
- **BUS (Business Logic Layer)**: Tầng xử lý nghiệp vụ mạng: tiếp nhận Socket, điều phối luồng, quản lý đa luồng (Multi-threading / ThreadPool), phân tích gói tin, định tuyến HTTP Router, Broadcast và Private message.
- **DAO (Data Access Object)**: Quản lý và lưu trữ dữ liệu người dùng (`UserDAO`), lịch sử tin nhắn (`ChatHistoryDAO`), danh sách sinh viên (`StudentDAO`), tệp tĩnh HTML/CSS (`StaticFileDAO`), và dữ liệu nhận từ POST Body (`MessageDAO`).
- **DTO (Data Transfer Object)**: Các đối tượng truyền tải dữ liệu gồm `ChatMessage`, `HttpRequest`, `HttpResponse`, `Student`, `User`, `RequestLog`.

---

## 🚀 2. HƯỚNG DẪN KHỞI CHẠY (RẤT DỄ SỬ DỤNG)

### Cách 1: Chạy nhanh bằng 1 cú click (Khuyên dùng trên Windows)
- **Mở ứng dụng chính**: Click đúp vào file [`run.bat`](file:///c:/Users/shuut/Documents/LTUDM/run.bat).
- **Mở thêm Client Chat độc lập**: Click đúp vào file [`run_client.bat`](file:///c:/Users/shuut/Documents/LTUDM/run_client.bat) (mỗi lần click sẽ mở thêm một Client mới để chat qua lại).

### Cách 2: Chạy thông qua Maven
```bash
# Biên dịch và chạy ứng dụng trực tiếp
mvn clean compile exec:java

# Hoặc đóng gói thành file JAR độc lập và chạy
mvn package
java -jar target/LTUDM_App-1.0.0.jar
```

### Cách 3: Chạy bộ kiểm thử tự động (Unit / Integration Tests)
```bash
mvn test
```
*(Đã vượt qua 8/8 ca kiểm thử tự động với kết quả 100% SUCCESS).*

---

## 💬 3. CHI TIẾT HOÀN THÀNH BÀI 11: TCP CHAT NHIỀU CLIENT

### A. Kiểm thử phòng chat
1. **Nhiều Client kết nối đồng thời**:
   - Sử dụng `ServerSocket` và `Thread` riêng biệt (`ClientHandler`) cho mỗi kết nối, hỗ trợ hàng chục Client kết nối đồng thời mà không bị nghẽn.
   - Nút bấm **"👥 Mở 4 Client Chat Mẫu"** trên giao diện tự động bật 4 Client (`Client #1001`, `Client #1002`, `Client #1003`, `Client #1004`) đúng y hệt hình vẽ đề bài để kiểm thử ngay lập tức.
2. **Tin nhắn được Broadcast đầy đủ**:
   - Tin nhắn gửi từ bất kỳ client nào sẽ được Server chuyển tiếp ngay lập tức đến toàn bộ các Client trong phòng chat.
3. **Client rời phòng không làm lỗi Server**:
   - Khi Client đóng cửa sổ hoặc mất kết nối, `ClientHandler` tự động giải phóng socket, xóa khỏi danh sách active users và thông báo cho các client khác, Server không bao giờ bị crash.
4. **Định dạng Log Server**:
   - Ghi nhận chính xác như ảnh mẫu:
     ```text
     [10:00:01] Client #1001 kết nối
     [10:00:02] Client #1002 kết nối
     [10:00:05] [#1002] Xin chào mọi người!
     ```

### B. Hoàn thành trọn vẹn 4 Bài tập mở rộng:
- **Bài mở rộng 1: Đặt tên người dùng (Username)**:
  - Cho phép người dùng tự đặt tên (như `Alice`, `Bob`...). Nếu để trống, Server sẽ tự động cấp mã định danh `Client #1001`, `Client #1002`...
  - Kiểm tra trùng tên: Nếu tên đã có người sử dụng, Server sẽ từ chối kết nối và thông báo cho người dùng chọn tên khác.
- **Bài mở rộng 2: Gửi tin nhắn riêng (Private Message / Whisper)**:
  - Người dùng có thể chọn người nhận từ menu dropdown hoặc click trực tiếp vào một người trong danh sách online.
  - Tin nhắn riêng hiển thị nổi bật với màu tím:
    - Phía người gửi: `[10:00:05] [Riêng tới Bob]: Chào Bob`
    - Phía người nhận: `[10:00:05] [Riêng từ Alice]: Chào Bob`
    - Các thành viên khác hoàn toàn không nhìn thấy nội dung này.
- **Bài mở rộng 3: Hiển thị thời gian gửi (Timestamp)**:
  - Tất cả tin nhắn đều có nhãn thời gian chuẩn `[HH:mm:ss]`.
- **Bài mở rộng 4: Danh sách người đang online (Online users list)**:
  - Danh sách người online được hiển thị ở sidebar bên phải với icon trạng thái xanh.
  - Cập nhật thời gian thực (Realtime): Mỗi khi có người vào hoặc ra, danh sách tự động đồng bộ trên toàn bộ các Client.

---

## 🌐 4. CHI TIẾT HOÀN THÀNH BÀI 12: HTTP VÀ HTTP SERVER

Tuân theo quy trình kiến trúc chuẩn:
$$\text{REQUEST} \longrightarrow \text{ROUTING} \longrightarrow \text{HANDLER} \longrightarrow \text{RESPONSE}$$

### A. Kiểm thử các ca cơ bản:
- **Máy chủ lắng nghe**: `http://localhost:8080/`
- **CA KIỂM THỬ 1**:
  - `GET /` $\rightarrow$ Trả về `200 OK`, `Content-Type: text/html`, Body: `<h1>HTTP Server</h1>`
- **CA KIỂM THỬ 2**:
  - `GET /api/time` $\rightarrow$ Trả về `200 OK`, `Content-Type: application/json`, Body: `{"time":"HH:mm:ss"}`
- **CA KIỂM THỬ 3**:
  - `GET /abc` $\rightarrow$ Trả về `404 Not Found`, `Content-Type: text/plain`, Body: `404 Not Found`

### B. Hoàn thành trọn vẹn 4 Bài tập mở rộng:
- **Bài mở rộng 1: Thêm POST `/api/message` - Đọc dữ liệu từ Request Body**:
  - `HttpParser` đọc Header `Content-Length` và trích xuất đúng số byte của Request Body từ luồng Socket InputStream.
  - Lưu nội dung vào `MessageDAO` và trả về JSON:
    `{"status":"success","received":"...","timestamp":"..."}`
- **Bài mở rộng 2: Trả danh sách sinh viên dạng JSON**:
  - Endpoint `GET /api/students` trả về mã phản hồi `200 OK`, `application/json` chứa mảng JSON các sinh viên từ `StudentDAO`.
  - Hỗ trợ thêm cả `POST /api/students` để thêm sinh viên mới trực tiếp qua Request Body JSON.
- **Bài mở rộng 3: Phục vụ tệp HTML và CSS tĩnh**:
  - Quản lý qua `StaticFileDAO`, phục vụ các tệp từ thư mục `www/` (như [`www/index.html`](file:///c:/Users/shuut/Documents/LTUDM/www/index.html) và [`www/style.css`](file:///c:/Users/shuut/Documents/LTUDM/www/style.css)).
  - Tự động map đúng MIME types: `.html` $\rightarrow$ `text/html`, `.css` $\rightarrow$ `text/css`, `.js` $\rightarrow$ `application/javascript`, v.v. Có cơ chế chống lỗ hổng Path Traversal (`..`).
- **Bài mở rộng 4: Xử lý nhiều Request đồng thời bằng Thread/Task (ThreadPool)**:
  - Máy chủ sử dụng `ExecutorService` (`Executors.newFixedThreadPool(30)`) để xử lý đồng thời nhiều kết nối HTTP, không làm nghẽn luồng lắng nghe.
- **Công cụ kiểm thử tích hợp (HTTP Client Tester / Mini Postman)**:
  - Tích hợp ngay trên giao diện Tab 2: Cho phép chọn Method (GET/POST), nhập URL, nhập Request Body, có 6 nút 1-Click Test Presets theo đúng từng ca trong đề bài, hiển thị huy hiệu Status (xanh nếu 200, đỏ nếu 404), Headers và Body.

---

## 📁 5. CẤU TRÚC THƯ MỤC DỰ ÁN

```text
c:\Users\shuut\Documents\LTUDM
├── pom.xml                                    # Cấu hình Maven hợp nhất
├── run.bat                                    # File chạy ứng dụng 1-Click trên Windows
├── run_client.bat                             # File mở thêm Client chat mới
├── README.md                                  # Tài liệu hướng dẫn chi tiết
├── www/                                       # Thư mục tệp tĩnh phục vụ HTTP Server
│   ├── index.html                             # Trang chủ HTML
│   └── style.css                              # Tệp định dạng CSS
└── src/
    ├── main/java/com/ltudm/
    │   ├── Main.java                          # Điểm khởi chạy ứng dụng hợp nhất
    │   ├── ClientLauncher.java                # Điểm khởi chạy Client độc lập
    │   │
    │   ├── dto/                               # DATA TRANSFER OBJECTS (MODEL)
    │   │   ├── ChatMessage.java               # Gói tin TCP Chat
    │   │   ├── MessageType.java               # Enum loại tin nhắn (CONNECT, PUBLIC, PRIVATE...)
    │   │   ├── User.java                      # Thông tin người dùng TCP
    │   │   ├── HttpRequest.java               # Gói tin HTTP Request (Method, Path, Headers, Body)
    │   │   ├── HttpResponse.java              # Gói tin HTTP Response (Status, Headers, Body)
    │   │   ├── Student.java                   # Đối tượng sinh viên
    │   │   └── RequestLog.java                # Đối tượng lưu lịch sử request HTTP
    │   │
    │   ├── dao/                               # DATA ACCESS OBJECTS
    │   │   ├── ChatHistoryDAO.java            # Lưu trữ và xuất lịch sử phòng chat
    │   │   ├── UserDAO.java                   # Quản lý người dùng TCP đang online
    │   │   ├── StudentDAO.java                # Quản lý dữ liệu sinh viên mẫu & thêm mới
    │   │   ├── StaticFileDAO.java             # Đọc và map MIME tệp tĩnh trong www/ an toàn
    │   │   └── MessageDAO.java                # Lưu tin nhắn nhận từ POST /api/message
    │   │
    │   ├── bus/                               # BUSINESS LOGIC LAYER
    │   │   ├── chat/                          # Nghiệp vụ TCP Chat (Bài 11)
    │   │   │   ├── ChatServerBUS.java         # Điều phối TCP Server, broadcast, whisper
    │   │   │   ├── ChatClientBUS.java         # Điều phối TCP Client, kết nối, nhận tin ngầm
    │   │   │   └── ClientHandler.java         # Xử lý Socket cho từng Client (Đa luồng)
    │   │   └── http/                          # Nghiệp vụ HTTP Server (Bài 12)
    │   │       ├── HttpServerBUS.java         # Điều phối HTTP Server, ThreadPool xử lý song song
    │   │       ├── HttpRouter.java            # Routing: Ánh xạ Method + Path tới Handler
    │   │       ├── HttpHandler.java           # Functional Interface cho bộ xử lý Request
    │   │       └── HttpParser.java            # Phân tích luồng Socket InputStream thành HttpRequest
    │   │
    │   └── gui/                               # GRAPHICAL USER INTERFACE
    │       ├── UITheme.java                   # Cấu hình màu sắc, FlatLaf Theme, Font, Bo góc
    │       ├── MainFrame.java                 # Cửa sổ chính hợp nhất toàn bộ hệ thống
    │       ├── DataManagementPanel.java       # Panel xem và quản lý dữ liệu tầng DAO
    │       ├── chat/                          # Giao diện Bài 11
    │       │   ├── TcpServerPanel.java        # Dashboard điều khiển TCP Server & danh sách Client
    │       │   └── TcpClientFrame.java        # Cửa sổ Client phòng chat đa tính năng
    │       └── http/                          # Giao diện Bài 12
    │           ├── HttpServerPanel.java       # Dashboard HTTP Server & Terminal Logs đen chuẩn mẫu
    │           └── HttpClientTesterPanel.java # Bộ kiểm thử HTTP (Mini-Postman) với 6 presets test
    │
    └── test/java/com/ltudm/
        └── FullSystemIntegrationTest.java     # Bộ 8 kiểm thử tự động toàn diện (JUnit 5)
```
