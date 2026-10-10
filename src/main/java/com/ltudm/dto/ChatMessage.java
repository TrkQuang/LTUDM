package com.ltudm.dto;

import com.google.gson.Gson;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DTO đại diện cho một thông điệp trao đổi trong phòng chat TCP.
 */
public class ChatMessage {
    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private MessageType type;
    private String sender;
    private String recipient;   // Dành cho tin nhắn riêng (PRIVATE), null nếu PUBLIC
    private String content;
    private String timestamp;   // Định dạng: [HH:mm:ss]
    private List<String> userList; // Danh sách online khi type = USER_LIST

    public ChatMessage() {
        this.timestamp = "[" + LocalTime.now().format(TIME_FORMATTER) + "]";
    }

    public ChatMessage(MessageType type, String sender, String content) {
        this();
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    public ChatMessage(MessageType type, String sender, String recipient, String content) {
        this();
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
    }

    // Factory methods tiện ích
    public static ChatMessage connect(String username) {
        return new ChatMessage(MessageType.CONNECT, username, "Yêu cầu kết nối");
    }

    public static ChatMessage publicMsg(String sender, String content) {
        return new ChatMessage(MessageType.PUBLIC, sender, content);
    }

    public static ChatMessage privateMsg(String sender, String recipient, String content) {
        return new ChatMessage(MessageType.PRIVATE, sender, recipient, content);
    }

    public static ChatMessage system(String content) {
        return new ChatMessage(MessageType.SYSTEM, "Hệ Thống", content);
    }

    public static ChatMessage userList(List<String> users) {
        ChatMessage msg = new ChatMessage(MessageType.USER_LIST, "Hệ Thống", "Cập nhật danh sách online");
        msg.setUserList(new ArrayList<>(users));
        return msg;
    }

    // Chuyển đối tượng thành chuỗi JSON
    public String toJson() {
        return GSON.toJson(this);
    }

    // Phân tích chuỗi JSON thành đối tượng ChatMessage
    public static ChatMessage fromJson(String json) {
        try {
            return GSON.fromJson(json, ChatMessage.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Getters and Setters
    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return timestamp + " " + (sender != null ? "[" + sender + "] " : "") + content;
    }
}
