package com.ltudm.bus.chat;

import com.ltudm.dao.ChatHistoryDAO;
import com.ltudm.dao.UserDAO;
import com.ltudm.dto.ChatMessage;
import com.ltudm.dto.MessageType;
import com.ltudm.dto.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tầng BUS điều phối toàn bộ nghiệp vụ của TCP Chat Server:
 * - Lắng nghe kết nối TCP
 * - Quản lý danh sách Client kết nối đồng thời
 * - Broadcast tin nhắn
 * - Chuyển tiếp tin nhắn riêng (Private Message)
 * - Tự động cập nhật danh sách người online (User List)
 * - Xử lý ngắt kết nối an toàn không làm crash Server
 */
public class ChatServerBUS {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clientMap = new ConcurrentHashMap<>();
    private final AtomicInteger clientIdCounter = new AtomicInteger(1001); // Client #1001, #1002...
    private final ChatHistoryDAO historyDAO = new ChatHistoryDAO();
    private final UserDAO userDAO = new UserDAO();
    private ExecutorService threadPool;
    private volatile boolean isRunning = false;
    private ServerEventListener listener;

    public interface ServerEventListener {
        void onServerStarted(int port);
        void onServerStopped();
        void onLog(String logMessage);
        void onClientListChanged(List<User> activeUsers);
        void onError(String errorMessage);
    }

    public void setListener(ServerEventListener listener) {
        this.listener = listener;
    }

    public synchronized boolean startServer(int port) {
        if (isRunning) return true;

        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newCachedThreadPool();
            isRunning = true;

            logSystem("Server đang chạy trên cổng " + port + "...");
            if (listener != null) {
                listener.onServerStarted(port);
            }

            threadPool.submit(() -> {
                while (isRunning && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        threadPool.submit(handler);
                    } catch (IOException e) {
                        if (!isRunning) break;
                    }
                }
            });

            return true;
        } catch (IOException e) {
            String err = "Không thể khởi động Server trên cổng " + port + ": " + e.getMessage();
            logSystem("[LỖI] " + err);
            if (listener != null) listener.onError(err);
            return false;
        }
    }

    public synchronized void stopServer() {
        if (!isRunning) return;
        isRunning = false;

        logSystem("Server đang dừng...");

        ChatMessage stopMsg = ChatMessage.system("Server đã dừng hoạt động.");
        broadcast(stopMsg);

        for (ClientHandler handler : clientMap.values()) {
            handler.closeConnection();
        }
        clientMap.clear();
        userDAO.clear();

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }

        logSystem("Server đã dừng hoàn toàn.");
        if (listener != null) {
            listener.onServerStopped();
            listener.onClientListChanged(Collections.emptyList());
        }
    }

    public synchronized String registerClient(ClientHandler handler, String requestedName) {
        String finalUsername;
        if (requestedName == null || requestedName.trim().isEmpty()) {
            finalUsername = "Client #" + clientIdCounter.getAndIncrement();
        } else {
            finalUsername = requestedName.trim();
            if (clientMap.containsKey(finalUsername.toLowerCase())) {
                return null;
            }
        }

        clientMap.put(finalUsername.toLowerCase(), handler);
        return finalUsername;
    }

    public void notifyClientJoined(ClientHandler handler) {
        User u = handler.getUser();
        userDAO.addUser(u);

        String time = LocalTime.now().format(TIME_FORMATTER);
        String logText = "[" + time + "] " + u.getUsername() + " kết nối";
        logSystem(logText);

        ChatMessage joinMsg = ChatMessage.system(u.getUsername() + " đã tham gia phòng chat!");
        historyDAO.saveMessage(joinMsg);
        broadcast(joinMsg);

        broadcastUserList();

        if (listener != null) {
            listener.onClientListChanged(userDAO.getAllUsers());
        }
    }

    public void broadcast(ChatMessage message) {
        if (message == null) return;

        historyDAO.saveMessage(message);

        if (message.getType() == MessageType.PUBLIC) {
            String logText = message.getTimestamp() + " [" + message.getSender() + "] " + message.getContent();
            logSystem(logText);
        }

        for (ClientHandler handler : clientMap.values()) {
            handler.sendMessage(message);
        }
    }

    public void sendPrivateMessage(ClientHandler senderHandler, ChatMessage message) {
        String recipientName = message.getRecipient();
        if (recipientName == null) return;

        ClientHandler recipientHandler = clientMap.get(recipientName.toLowerCase());

        if (recipientHandler != null) {
            recipientHandler.sendMessage(message);
            senderHandler.sendMessage(message);

            historyDAO.saveMessage(message);

            String logText = message.getTimestamp() + " [RIÊNG] [" + message.getSender() + " -> " + recipientName + "]: " + message.getContent();
            logSystem(logText);
        } else {
            ChatMessage notFound = ChatMessage.system("Người dùng '" + recipientName + "' hiện không trực tuyến.");
            senderHandler.sendMessage(notFound);
        }
    }

    public synchronized void handleClientDisconnect(ClientHandler handler) {
        if (handler == null || handler.getUsername() == null) return;

        String username = handler.getUsername();
        if (clientMap.remove(username.toLowerCase()) != null) {
            userDAO.removeUser(username);

            String time = LocalTime.now().format(TIME_FORMATTER);
            String logText = "[" + time + "] " + username + " đã ngắt kết nối";
            logSystem(logText);

            ChatMessage leaveMsg = ChatMessage.system(username + " đã rời phòng chat.");
            historyDAO.saveMessage(leaveMsg);
            broadcast(leaveMsg);

            broadcastUserList();

            if (listener != null) {
                listener.onClientListChanged(userDAO.getAllUsers());
            }
        }
    }

    public void broadcastUserList() {
        List<String> usernames = new ArrayList<>();
        for (ClientHandler h : clientMap.values()) {
            usernames.add(h.getUsername());
        }
        Collections.sort(usernames);

        ChatMessage listMsg = ChatMessage.userList(usernames);
        for (ClientHandler h : clientMap.values()) {
            h.sendMessage(listMsg);
        }
    }

    private void logSystem(String message) {
        if (listener != null) {
            listener.onLog(message);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public ChatHistoryDAO getHistoryDAO() {
        return historyDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }
}
