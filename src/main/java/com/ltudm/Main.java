package com.ltudm;

import com.ltudm.gui.MainFrame;
import com.ltudm.gui.UITheme;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Điểm khởi chạy chính của Ứng Dụng Mạng Hợp Nhất (LTUDM)
 * Bao gồm cả Bài 11 (TCP Chat) và Bài 12 (HTTP Server).
 */
public class Main {
    public static void main(String[] args) {
        // Nếu chạy trong môi trường headless (Server/Docker Linux không màn hình) hoặc có cờ --server
        if (GraphicsEnvironment.isHeadless() || (args.length > 0 && "--server".equalsIgnoreCase(args[0]))) {
            ServerLauncher.main(args);
            return;
        }

        // Môi trường Desktop thông thường: Khởi chạy GUI Swing
        UITheme.setupFlatLaf();

        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}
