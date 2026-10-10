package com.ltudm.bus.chat;

import com.ltudm.dto.ChatMessage;
import com.ltudm.dto.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Tầng BUS điều phối kết nối và truyền nhận dữ liệu cho Chat Client:
 * - Kết nối Socket tới Server
 * - Gửi tin nhắn công khai / tin nhắn riêng
 * - Nhận và phân luồng thông điệp từ Server về giao diện GUI
 * - Ngắt kết nối an toàn
 */
public class ChatClientBUS {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String currentUsername;
    private volatile boolean isConnected = false;
    private Thread receiveThread;
    private ClientEventListener listener;

    public interface ClientEventListener {
        void onConnected(String myUsername, String host, int port);
        void onDisconnected();
        void onMessageReceived(ChatMessage message);
        void onUserListUpdated(List<String> users);
        void onError(String errorMessage);
    }

    public void setListener(ClientEventListener listener) {
        this.listener = listener;
    }

    public boolean connect(String host, int port, String desiredUsername) {
        if (isConnected) return true;

        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            ChatMessage connectMsg = ChatMessage.connect(desiredUsername);
            writer.println(connectMsg.toJson());

            String responseLine = reader.readLine();
            if (responseLine == null) {
                closeConnection();
                if (listener != null) listener.onError("Server đã đóng kết nối đột ngột.");
                return false;
            }

            ChatMessage resp = ChatMessage.fromJson(responseLine);
            if (resp == null || resp.getType() != MessageType.CONNECT_OK) {
                String errMsg = (resp != null) ? resp.getContent() : "Kết nối bị từ chối.";
                closeConnection();
                if (listener != null) listener.onError(errMsg);
                return false;
            }

            this.currentUsername = resp.getContent();
            this.isConnected = true;

            if (listener != null) {
                listener.onConnected(currentUsername, host, port);
            }

            startReceiverThread();
            return true;
        } catch (IOException e) {
            closeConnection();
            String err = "Không thể kết nối đến " + host + ":" + port + " (" + e.getMessage() + ")";
            if (listener != null) listener.onError(err);
            return false;
        }
    }

    private void startReceiverThread() {
        receiveThread = new Thread(() -> {
            try {
                String line;
                while (isConnected && (line = reader.readLine()) != null) {
                    ChatMessage msg = ChatMessage.fromJson(line);
                    if (msg == null) continue;

                    if (listener != null) {
                        if (msg.getType() == MessageType.USER_LIST) {
                            listener.onUserListUpdated(msg.getUserList());
                        } else {
                            listener.onMessageReceived(msg);
                        }
                    }
                }
            } catch (IOException ignored) {
            } finally {
                disconnect();
            }
        });
        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    public boolean sendPublicMessage(String content) {
        if (!isConnected || content == null || content.trim().isEmpty()) return false;
        ChatMessage msg = ChatMessage.publicMsg(currentUsername, content.trim());
        return sendMessage(msg);
    }

    public boolean sendPrivateMessage(String recipient, String content) {
        if (!isConnected || content == null || content.trim().isEmpty() || recipient == null) return false;
        ChatMessage msg = ChatMessage.privateMsg(currentUsername, recipient.trim(), content.trim());
        return sendMessage(msg);
    }

    private synchronized boolean sendMessage(ChatMessage message) {
        if (writer != null && !socket.isClosed()) {
            writer.println(message.toJson());
            return true;
        }
        return false;
    }

    public void disconnect() {
        if (!isConnected) return;
        isConnected = false;

        try {
            if (writer != null && !socket.isClosed()) {
                ChatMessage leaveMsg = new ChatMessage(MessageType.LEAVE, currentUsername, "Rời phòng");
                writer.println(leaveMsg.toJson());
            }
        } catch (Exception ignored) {}

        closeConnection();

        if (listener != null) {
            listener.onDisconnected();
        }
    }

    private void closeConnection() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }
}
