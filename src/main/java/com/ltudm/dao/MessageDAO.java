package com.ltudm.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tầng DAO lưu trữ các tin nhắn nhận được từ HTTP POST /api/message
 * (Bài 12 - Bài tập mở rộng #1).
 */
public class MessageDAO {
    public static class ReceivedMessage {
        private final String timestamp;
        private final String body;
        private final String remoteAddress;

        public ReceivedMessage(String body, String remoteAddress) {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy"));
            this.body = body;
            this.remoteAddress = remoteAddress;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getBody() {
            return body;
        }

        public String getRemoteAddress() {
            return remoteAddress;
        }
    }

    private final List<ReceivedMessage> messageList = Collections.synchronizedList(new ArrayList<>());

    public void addMessage(String body, String remoteAddress) {
        messageList.add(new ReceivedMessage(body, remoteAddress));
    }

    public List<ReceivedMessage> getAll() {
        return new ArrayList<>(messageList);
    }

    public int count() {
        return messageList.size();
    }

    public void clear() {
        messageList.clear();
    }
}
