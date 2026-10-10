package com.ltudm.gui.http;

import com.ltudm.gui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Giao diện Công cụ Kiểm thử HTTP (HTTP Client Tester / Mini Postman)
 * Tương đương nhánh giao tiếp từ Firefox (hoặc Web Client) -> IIS (Web Server).
 * - Chọn Method: GET, POST
 * - Nhập URL
 * - Nhập Request Body (khi gửi POST)
 * - Nút "Send" gửi yêu cầu HTTP thực sự
 * - Các nút kiểm thử nhanh 1-Click
 * - Hiển thị huy hiệu Trạng thái (200 OK / 404 Not Found) + Headers + Body
 */
public class HttpClientTesterPanel extends JPanel {
    private JComboBox<String> cbMethod;
    private JTextField txtUrl;
    private JButton btnSend;

    private JPanel bodyPanel;
    private JTextArea txtRequestBody;

    private JLabel lblStatusBadge;
    private JTextArea txtResponseHeaders;
    private JTextArea txtResponseBody;

    public HttpClientTesterPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. THANH ĐIỀU KHIỂN GỬI REQUEST
        JPanel requestCard = UITheme.createCard();
        requestCard.setLayout(new BorderLayout(0, 10));

        JPanel urlBar = new JPanel(new BorderLayout(8, 0));
        urlBar.setOpaque(false);

        cbMethod = new JComboBox<>(new String[]{"GET", "POST"});
        cbMethod.setFont(UITheme.FONT_BOLD);
        cbMethod.setPreferredSize(new Dimension(90, 36));
        cbMethod.addActionListener(e -> updateBodyVisibility());

        txtUrl = new JTextField("http://localhost:8080/");
        txtUrl.setFont(UITheme.FONT_REGULAR);
        txtUrl.setBorder(UITheme.createFieldBorder());
        txtUrl.setPreferredSize(new Dimension(400, 36));

        btnSend = UITheme.createPrimaryButton("Send");
        btnSend.setPreferredSize(new Dimension(85, 36));
        btnSend.addActionListener(e -> sendHttpRequest());

        urlBar.add(cbMethod, BorderLayout.WEST);
        urlBar.add(txtUrl, BorderLayout.CENTER);
        urlBar.add(btnSend, BorderLayout.EAST);

        // Vùng nhập Request Body (Tự động mở khi chọn POST)
        bodyPanel = new JPanel(new BorderLayout(0, 4));
        bodyPanel.setOpaque(false);
        bodyPanel.setVisible(false);

        JLabel lblBodyTitle = new JLabel("Request Body (Dữ liệu gửi lên Server):");
        lblBodyTitle.setFont(UITheme.FONT_BOLD);

        txtRequestBody = new JTextArea(4, 30);
        txtRequestBody.setFont(UITheme.FONT_MONO);
        txtRequestBody.setText("{\n  \"message\": \"Xin chào HTTP Server từ Client!\"\n}");
        txtRequestBody.setMargin(new Insets(6, 6, 6, 6));

        JScrollPane scrollBody = new JScrollPane(txtRequestBody);
        scrollBody.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        bodyPanel.add(lblBodyTitle, BorderLayout.NORTH);
        bodyPanel.add(scrollBody, BorderLayout.CENTER);

        // Các nút chạy nhanh Preset
        JPanel presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        presetPanel.setOpaque(false);

        JLabel lblPresets = new JLabel("Test nhanh:");
        lblPresets.setFont(UITheme.FONT_BOLD);
        presetPanel.add(lblPresets);

        JButton btnTest1 = createPresetBtn("Ca 1: GET /", "GET", "http://localhost:8080/", null);
        JButton btnTest2 = createPresetBtn("Ca 2: GET /api/time", "GET", "http://localhost:8080/api/time", null);
        JButton btnTest3 = createPresetBtn("Ca 3: GET /abc (404)", "GET", "http://localhost:8080/abc", null);
        JButton btnTest4 = createPresetBtn("Mở rộng 1: POST /api/message", "POST", "http://localhost:8080/api/message", "{\n  \"sender\": \"SinhVien\",\n  \"content\": \"Kiểm thử đọc dữ liệu Request Body\"\n}");
        JButton btnTest5 = createPresetBtn("Mở rộng 2: GET /api/students", "GET", "http://localhost:8080/api/students", null);
        JButton btnTest6 = createPresetBtn("Mở rộng 3: GET /style.css", "GET", "http://localhost:8080/style.css", null);

        presetPanel.add(btnTest1);
        presetPanel.add(btnTest2);
        presetPanel.add(btnTest3);
        presetPanel.add(btnTest4);
        presetPanel.add(btnTest5);
        presetPanel.add(btnTest6);

        requestCard.add(urlBar, BorderLayout.NORTH);
        requestCard.add(bodyPanel, BorderLayout.CENTER);
        requestCard.add(presetPanel, BorderLayout.SOUTH);

        // 2. KHU VỰC HIỂN THỊ KẾT QUẢ RESPONSE
        JPanel responseCard = UITheme.createCard();
        responseCard.setLayout(new BorderLayout(0, 8));

        JPanel statusHeader = new JPanel(new BorderLayout());
        statusHeader.setOpaque(false);

        JLabel lblRespTitle = new JLabel("Phản Hồi (Response)");
        lblRespTitle.setFont(UITheme.FONT_HEADER);

        lblStatusBadge = new JLabel("Chưa có phản hồi", SwingConstants.CENTER);
        lblStatusBadge.setFont(UITheme.FONT_BOLD);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(UITheme.TEXT_MUTED);
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setBorder(new EmptyBorder(5, 14, 5, 14));

        statusHeader.add(lblRespTitle, BorderLayout.WEST);
        statusHeader.add(lblStatusBadge, BorderLayout.EAST);

        // Chia Response thành 2 Tabs: Response Body và Response Headers
        JTabbedPane respTabs = new JTabbedPane();
        respTabs.setFont(UITheme.FONT_BOLD);

        txtResponseBody = new JTextArea();
        txtResponseBody.setEditable(false);
        txtResponseBody.setFont(UITheme.FONT_MONO);
        txtResponseBody.setMargin(new Insets(8, 8, 8, 8));
        txtResponseBody.setBackground(new Color(250, 250, 250));

        JScrollPane scrollRespBody = new JScrollPane(txtResponseBody);
        scrollRespBody.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        txtResponseHeaders = new JTextArea();
        txtResponseHeaders.setEditable(false);
        txtResponseHeaders.setFont(UITheme.FONT_MONO);
        txtResponseHeaders.setMargin(new Insets(8, 8, 8, 8));

        JScrollPane scrollRespHeaders = new JScrollPane(txtResponseHeaders);
        scrollRespHeaders.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        respTabs.addTab("Response Body", scrollRespBody);
        respTabs.addTab("Headers", scrollRespHeaders);

        responseCard.add(statusHeader, BorderLayout.NORTH);
        responseCard.add(respTabs, BorderLayout.CENTER);

        add(requestCard, BorderLayout.NORTH);
        add(responseCard, BorderLayout.CENTER);
    }

    private JButton createPresetBtn(String label, String method, String url, String body) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            cbMethod.setSelectedItem(method);
            txtUrl.setText(url);
            if (body != null) {
                txtRequestBody.setText(body);
            }
            updateBodyVisibility();
            sendHttpRequest();
        });
        return btn;
    }

    private void updateBodyVisibility() {
        boolean isPost = "POST".equalsIgnoreCase((String) cbMethod.getSelectedItem());
        bodyPanel.setVisible(isPost);
        revalidate();
        repaint();
    }

    /**
     * Thực hiện gửi yêu cầu HTTP thực qua HttpURLConnection chạy ngầm trên Thread.
     */
    private void sendHttpRequest() {
        String method = (String) cbMethod.getSelectedItem();
        String urlString = txtUrl.getText().trim();
        String requestBody = txtRequestBody.getText();

        btnSend.setEnabled(false);
        lblStatusBadge.setBackground(UITheme.PRIMARY);
        lblStatusBadge.setText("Đang gửi yêu cầu...");

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if ("POST".equalsIgnoreCase(method)) {
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    byte[] outBytes = requestBody.getBytes(StandardCharsets.UTF_8);
                    conn.setRequestProperty("Content-Length", String.valueOf(outBytes.length));

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(outBytes);
                        os.flush();
                    }
                }

                int statusCode = conn.getResponseCode();
                String statusMsg = conn.getResponseMessage();
                String contentType = conn.getContentType();

                // Đọc Headers
                StringBuilder headersSb = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null) {
                        headersSb.append(entry.getKey()).append(": ").append(String.join(", ", entry.getValue())).append("\n");
                    }
                }

                // Đọc Body (từ getInputStream nếu 2xx, hoặc getErrorStream nếu 4xx/5xx)
                InputStream is = (statusCode >= 200 && statusCode < 400) ? conn.getInputStream() : conn.getErrorStream();
                StringBuilder bodySb = new StringBuilder();
                if (is != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            bodySb.append(line).append("\n");
                        }
                    }
                }

                String finalBody = bodySb.toString();
                String finalHeaders = headersSb.toString();

                SwingUtilities.invokeLater(() -> {
                    btnSend.setEnabled(true);
                    if (statusCode == 200) {
                        lblStatusBadge.setBackground(UITheme.SUCCESS);
                        lblStatusBadge.setText("✓ " + statusCode + " " + (statusMsg != null ? statusMsg : "OK") + " • " + (contentType != null ? contentType : ""));
                    } else if (statusCode == 404) {
                        lblStatusBadge.setBackground(UITheme.DANGER);
                        lblStatusBadge.setText("✗ " + statusCode + " " + (statusMsg != null ? statusMsg : "Not Found") + " • " + (contentType != null ? contentType : ""));
                    } else {
                        lblStatusBadge.setBackground(UITheme.WARNING);
                        lblStatusBadge.setText(statusCode + " " + (statusMsg != null ? statusMsg : "") + " • " + (contentType != null ? contentType : ""));
                    }

                    txtResponseHeaders.setText(finalHeaders);
                    txtResponseBody.setText(finalBody);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    btnSend.setEnabled(true);
                    lblStatusBadge.setBackground(UITheme.DANGER);
                    lblStatusBadge.setText("Lỗi kết nối");
                    txtResponseBody.setText("Không thể kết nối đến máy chủ:\n" + ex.getMessage() + "\n\n(Hãy chắc chắn rằng HTTP Server đã được khởi động ở Tab bên cạnh!)");
                    txtResponseHeaders.setText("");
                });
            }
        }).start();
    }
}
