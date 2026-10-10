package com.ltudm;

import com.ltudm.gui.UITheme;
import com.ltudm.gui.chat.TcpClientPanel;
import com.ltudm.gui.http.HttpClientTesterPanel;
import javax.swing.*;
import java.awt.*;

/**
 * Khởi chạy độc lập ứng dụng Client, bao gồm cả TCP Client và HTTP Client
 * tương ứng với hình vẽ luồng hệ thống.
 */
public class ClientLauncher {
    public static void main(String[] args) {
        UITheme.setupFlatLaf();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LTUDM Client Application");
            frame.setSize(1000, 700);
            frame.setMinimumSize(new Dimension(800, 500));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(UITheme.FONT_BOLD);
            
            TcpClientPanel tcpClientPanel = new TcpClientPanel();
            HttpClientTesterPanel httpClientPanel = new HttpClientTesterPanel();
            
            tabbedPane.addTab("TCP Client (Giao tiếp với Chat Server)", tcpClientPanel);
            tabbedPane.addTab("Web Client (Giao tiếp với HTTP Server)", httpClientPanel);
            
            frame.setContentPane(tabbedPane);
            frame.setVisible(true);
        });
    }
}
