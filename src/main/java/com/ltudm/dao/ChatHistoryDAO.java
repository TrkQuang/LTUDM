package com.ltudm.dao;

import com.ltudm.dto.ChatMessage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Tầng DAO quản lý lưu trữ và xuất lịch sử phòng chat TCP.
 */
public class ChatHistoryDAO {
    private static final String DEFAULT_LOG_FILE = "logs/chat_history.log";
    private final List<ChatMessage> historyList = Collections.synchronizedList(new ArrayList<>());

    public ChatHistoryDAO() {
        File dir = new File("logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public synchronized void saveMessage(ChatMessage message) {
        if (message == null) return;
        historyList.add(message);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(DEFAULT_LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.write(message.toString());
            writer.newLine();
        } catch (IOException ignored) {}
    }

    public List<ChatMessage> getHistory() {
        return new ArrayList<>(historyList);
    }

    public synchronized boolean exportToFile(File targetFile) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {
            writer.write("==================================================");
            writer.newLine();
            writer.write("          LỊCH SỬ PHÒNG CHAT TCP (BÀI 11)         ");
            writer.newLine();
            writer.write("==================================================");
            writer.newLine();

            for (ChatMessage msg : historyList) {
                writer.write(msg.toString());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void clear() {
        historyList.clear();
    }
}
