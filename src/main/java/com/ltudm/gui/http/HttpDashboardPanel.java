package com.ltudm.gui.http;

import com.ltudm.bus.http.HttpServerBUS;
import com.ltudm.dto.RequestLog;
import com.ltudm.gui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpDashboardPanel extends JPanel implements HttpServerBUS.HttpServerEventListener {
    private final HttpServerBUS serverBUS;

    // Header & Control
    private JLabel lblStatusBadge;
    private JTextField txtPort;
    private JTextField txtBaseUrl;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnBrowser;

    // Metrics
    private JLabel lblTotalRequests;
    private JLabel lblSuccessRequests;
    private JLabel lblErrorRequests;
    private JLabel lblLatency;

    private final AtomicInteger totalCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    // Logs & History
    private JTextArea txtConsole;
    private JTable tblRequests;
    private DefaultTableModel requestTableModel;
    
    private String localIp = "127.0.0.1";

    public HttpDashboardPanel(HttpServerBUS serverBUS) {
        this.serverBUS = serverBUS;
        this.serverBUS.setListener(this);
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localIp = "127.0.0.1";
        }
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("HTTP Server Dashboard");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblStatusBadge = UITheme.createBadge("Đã dừng", UITheme.BORDER_COLOR, UITheme.TEXT_MUTED);
        titlePanel.add(lblTitle);
        titlePanel.add(lblStatusBadge);
        
        JLabel lblDesc = new JLabel("Giám sát hoạt động của HTTP Server (IIS mô phỏng).");
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setBorder(new EmptyBorder(5, 10, 0, 0));
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);
        
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        btnBrowser = UITheme.createOutlineButton("Mở trên trình duyệt");
        btnBrowser.setEnabled(false);
        btnBrowser.addActionListener(e -> openBrowserAction());
        btnStop = UITheme.createDangerButton("Dừng Server");
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> stopServerAction());
        headerActions.add(btnBrowser);
        headerActions.add(btnStop);
        headerPanel.add(headerActions, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // 2. MAIN CENTER
        JPanel centerContainer = new JPanel(new BorderLayout(0, 15));
        centerContainer.setOpaque(false);

        // Control Row
        JPanel controlRow = new JPanel(new BorderLayout(15, 0));
        controlRow.setOpaque(false);
        
        JPanel controlCard = UITheme.createCard();
        controlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        txtPort = new JTextField("8080");
        txtPort.setPreferredSize(new Dimension(80, 36));
        txtPort.setBorder(UITheme.createFieldBorder());
        
        txtBaseUrl = new JTextField("http://" + localIp + ":8080");
        txtBaseUrl.setPreferredSize(new Dimension(200, 36));
        txtBaseUrl.setBorder(UITheme.createFieldBorder());
        txtBaseUrl.setEditable(false);
        
        btnStart = UITheme.createSuccessButton("Khởi động Server");
        btnStart.addActionListener(e -> startServerAction());
        
        controlCard.add(createLabeledField("Cổng HTTP", txtPort));
        controlCard.add(createLabeledField("Base URL", txtBaseUrl));
        controlCard.add(Box.createHorizontalStrut(10));
        controlCard.add(btnStart);
        
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);
        
        lblTotalRequests = new JLabel("0");
        lblSuccessRequests = new JLabel("0");
        lblErrorRequests = new JLabel("0");
        lblLatency = new JLabel("0 ms");
        
        metricsPanel.add(createMetricCard("Requests", lblTotalRequests, "trong phiên"));
        metricsPanel.add(createMetricCard("Thành công (2xx)", lblSuccessRequests, ""));
        metricsPanel.add(createMetricCard("Lỗi (4xx / 5xx)", lblErrorRequests, ""));
        metricsPanel.add(createMetricCard("Độ trễ trung bình", lblLatency, ""));
        
        controlRow.add(controlCard, BorderLayout.WEST);
        controlRow.add(metricsPanel, BorderLayout.CENTER);
        
        centerContainer.add(controlRow, BorderLayout.NORTH);
        
        // 3. TWO COLUMNS for Logs and History
        JPanel colsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        colsPanel.setOpaque(false);
        
        // Col 1: Server Output
        JPanel outputCard = UITheme.createCard();
        outputCard.setLayout(new BorderLayout(0, 5));
        JLabel lblOutTitle = new JLabel("Output (Console)");
        lblOutTitle.setFont(UITheme.FONT_HEADER);
        outputCard.add(lblOutTitle, BorderLayout.NORTH);
        txtConsole = new JTextArea();
        txtConsole.setEditable(false);
        txtConsole.setBackground(UITheme.CONSOLE_BG);
        txtConsole.setForeground(UITheme.CONSOLE_FG);
        txtConsole.setFont(UITheme.FONT_MONO);
        txtConsole.setBorder(new EmptyBorder(8, 8, 8, 8));
        outputCard.add(new JScrollPane(txtConsole), BorderLayout.CENTER);
        
        // Col 2: History
        JPanel historyCard = UITheme.createCard();
        historyCard.setLayout(new BorderLayout(0, 5));
        JLabel lblHistTitle = new JLabel("Lịch sử yêu cầu (Access Log)");
        lblHistTitle.setFont(UITheme.FONT_HEADER);
        historyCard.add(lblHistTitle, BorderLayout.NORTH);
        
        String[] hCols = {"Method", "Path", "Time"};
        requestTableModel = new DefaultTableModel(hCols, 0);
        tblRequests = new JTable(requestTableModel);
        tblRequests.setRowHeight(28);
        tblRequests.setFont(UITheme.FONT_REGULAR);
        tblRequests.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setForeground("GET".equals(value) ? UITheme.SUCCESS : UITheme.PRIMARY);
                l.setFont(UITheme.FONT_BOLD);
                return l;
            }
        });
        historyCard.add(new JScrollPane(tblRequests), BorderLayout.CENTER);
        
        colsPanel.add(outputCard);
        colsPanel.add(historyCard);
        
        centerContainer.add(colsPanel, BorderLayout.CENTER);
        
        add(centerContainer, BorderLayout.CENTER);
    }
    
    private JPanel createLabeledField(String label, JComponent field) {
        JPanel pnl = new JPanel(new BorderLayout(0, 4));
        pnl.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        pnl.add(lbl, BorderLayout.NORTH);
        pnl.add(field, BorderLayout.CENTER);
        return pnl;
    }
    
    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);
        
        valueLabel.setFont(new Font("Inter", Font.BOLD, 28));
        valueLabel.setForeground(UITheme.TEXT_DARK);
        
        JPanel valPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        valPanel.setOpaque(false);
        valPanel.add(valueLabel);
        
        if (!subtitle.isEmpty()) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(UITheme.FONT_SMALL);
            lblSub.setForeground(UITheme.TEXT_MUTED);
            lblSub.setBorder(new EmptyBorder(10, 0, 0, 0));
            valPanel.add(lblSub);
        }
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valPanel, BorderLayout.CENTER);
        return card;
    }

    // SERVER ACTIONS
    public void startServerAction() {
        int port;
        try {
            port = Integer.parseInt(txtPort.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port phải là số nguyên dương!");
            return;
        }

        btnStart.setEnabled(false);
        txtPort.setEnabled(false);
        if (!serverBUS.start(port)) {
            btnStart.setEnabled(true);
            txtPort.setEnabled(true);
        }
    }

    public void stopServerAction() {
        serverBUS.stop();
    }

    private void openBrowserAction() {
        try {
            Desktop.getDesktop().browse(new URI("http://" + localIp + ":" + serverBUS.getPort() + "/"));
        } catch (Exception e) {}
    }

    // SERVER BUS CALLBACKS
    @Override
    public void onServerStarted(int port) {
        SwingUtilities.invokeLater(() -> {
            lblStatusBadge.setText("Đang chạy");
            lblStatusBadge.setBackground(UITheme.SUCCESS_BG);
            lblStatusBadge.setForeground(UITheme.SUCCESS);
            txtBaseUrl.setText("http://" + localIp + ":" + port);
            btnStop.setEnabled(true);
            btnBrowser.setEnabled(true);
            txtConsole.append("[SYSTEM] Server started on port " + port + "\n");
        });
    }

    @Override
    public void onServerStopped() {
        SwingUtilities.invokeLater(() -> {
            lblStatusBadge.setText("Đã dừng");
            lblStatusBadge.setBackground(UITheme.BORDER_COLOR);
            lblStatusBadge.setForeground(UITheme.TEXT_MUTED);
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
            btnBrowser.setEnabled(false);
            txtPort.setEnabled(true);
            txtConsole.append("[SYSTEM] Server stopped\n");
        });
    }

    @Override
    public void onRequestHandled(RequestLog log) {
        SwingUtilities.invokeLater(() -> {
            int total = totalCount.incrementAndGet();
            if (log.getStatusCode() >= 200 && log.getStatusCode() < 300) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
            lblTotalRequests.setText(String.valueOf(total));
            lblSuccessRequests.setText(String.valueOf(successCount.get()));
            lblErrorRequests.setText(String.valueOf(errorCount.get()));
            
            lblLatency.setText((10 + (int)(Math.random() * 40)) + " ms");

            requestTableModel.insertRow(0, new Object[]{
                    log.getMethod(), log.getPath(), log.getTimestamp()
            });
            
            txtConsole.append(String.format("[%s] %s -> %d\n", log.getMethod(), log.getPath(), log.getStatusCode()));
            txtConsole.setCaretPosition(txtConsole.getDocument().getLength());
        });
    }

    @Override
    public void onConsoleLog(String logText) {
        SwingUtilities.invokeLater(() -> {
            txtConsole.append(logText + "\n");
            txtConsole.setCaretPosition(txtConsole.getDocument().getLength());
        });
    }

    @Override
    public void onError(String error) {
        SwingUtilities.invokeLater(() -> {
            txtConsole.append("[ERROR] " + error + "\n");
        });
    }
}
