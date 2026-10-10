package com.ltudm.bus.chat;

import com.ltudm.dto.ChatMessage;
import com.ltudm.dto.MessageType;
import com.ltudm.dto.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Xử lý luồng giao tiếp TCP riêng biệt cho từng Client kết nối đến Server.
 * Chạy trên một Thread độc lập, đảm bảo nhiều Client kết nối đồng thời không bị nghẽn.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServerBUS serverBUS;
    private BufferedReader reader;
    private PrintWriter writer;
    private User user;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, ChatServerBUS serverBUS) {
        this.socket = socket;
        this.serverBUS = serverBUS;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            // Bước 1: Đợi thông điệp CONNECT từ Client để thiết lập Username / Client ID
            String firstLine = reader.readLine();
            if (firstLine == null) {
                closeConnection();
                return;
            }

            ChatMessage firstMsg = ChatMessage.fromJson(firstLine);
            String requestedName = (firstMsg != null && firstMsg.getSender() != null) 
                    ? firstMsg.getSender().trim() : null;

            // Đăng ký client với ServerBUS
            String assignedUsername = serverBUS.registerClient(this, requestedName);
            if (assignedUsername == null) {
                // Tên bị trùng hoặc không hợp lệ -> gửi thông báo từ chối
                ChatMessage failMsg = new ChatMessage(MessageType.CONNECT_FAIL, "Server", "Tên người dùng đã được sử dụng!");
                sendMessage(failMsg);
                closeConnection();
                return;
            }

            String ip = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            this.user = new User(assignedUsername, assignedUsername, ip, port);

            // Gửi xác nhận kết nối thành công về cho client
            ChatMessage okMsg = new ChatMessage(MessageType.CONNECT_OK, "Server", assignedUsername);
            sendMessage(okMsg);

            // Server thông báo cho toàn bộ phòng chat biết có người mới vào
            serverBUS.notifyClientJoined(this);

            // Bước 2: Vòng lặp nhận tin nhắn từ Client
            String line;
            while (running && (line = reader.readLine()) != null) {
                ChatMessage message = ChatMessage.fromJson(line);
                if (message == null) continue;

                message.setSender(this.user.getUsername());

                switch (message.getType()) {
                    case PUBLIC:
                        // Broadcast tin nhắn tới tất cả mọi người
                        serverBUS.broadcast(message);
                        break;

                    case PRIVATE:
                        // Tin nhắn riêng (Whisper) tới người nhận cụ thể
                        serverBUS.sendPrivateMessage(this, message);
                        break;

                    case LEAVE:
                        running = false;
                        break;

                    default:
                        break;
                }
            }
        } catch (IOException ignored) {
            // Mất kết nối hoặc socket bị đóng
        } finally {
            closeConnection();
            serverBUS.handleClientDisconnect(this);
        }
    }

    public synchronized void sendMessage(ChatMessage message) {
        if (writer != null && !socket.isClosed()) {
            writer.println(message.toJson());
        }
    }

    public void closeConnection() {
        running = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public User getUser() {
        return user;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : "";
    }

    public Socket getSocket() {
        return socket;
    }
}
