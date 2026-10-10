package com.ltudm.gui.chat;

import com.ltudm.bus.chat.ChatServerBUS;
import com.ltudm.gui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import com.ltudm.dto.User;
import com.ltudm.dto.ChatMessage;

public class TcpServerPanel extends JPanel implements ChatServerBUS.ServerEventListener {
    private final ChatServerBUS chatServerBUS;

    private JLabel lblStatusBadge;
    private JTextField txtPort;
    private JTextField txtBindHost;
    private JButton btnStart;
    private JButton btnStop;

    private JLabel lblClientCount;
    private JLabel lblMessageCount;
    private JLabel lblUptime;
    private JLabel lblErrorCount;

    private JTextPane consolePane;
    private StyledDocument consoleDoc;
    private JCheckBox chkAutoScroll;

    private JTable tblClients;
    private DefaultTableModel clientTableModel;

    private Timer uptimeTimer;
    private long startTime;
    private int errorCount = 0;
    private String localIp = "0.0.0.0";

    public TcpServerPanel(ChatServerBUS chatServerBUS) {
        this.chatServerBUS = chatServerBUS;
        this.chatServerBUS.setListener(this);
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localIp = "0.0.0.0";
        }
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("TCP Chat Server");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        
        lblStatusBadge = UITheme.createBadge("Đã dừng", UITheme.BORDER_COLOR, UITheme.TEXT_MUTED);
        
        titlePanel.add(lblTitle);
        titlePanel.add(lblStatusBadge);
        
        JLabel lblDesc = new JLabel("Khởi chạy ServerSocket, quan sát kết nối đồng thời và theo dõi luồng broadcast.");
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setBorder(new EmptyBorder(5, 10, 0, 0));
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);

        // Header Actions removed as client is now a separate application

        add(headerPanel, BorderLayout.NORTH);

        // 2. Control and Metrics
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        // Control Card
        JPanel controlCard = UITheme.createCard();
        controlCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        
        txtPort = new JTextField("8888");
        txtPort.setPreferredSize(new Dimension(80, 36));
        txtPort.setBorder(UITheme.createFieldBorder());
        
        txtBindHost = new JTextField(localIp);
        txtBindHost.setPreferredSize(new Dimension(120, 36));
        txtBindHost.setBorder(UITheme.createFieldBorder());
        txtBindHost.setEditable(false);
        txtBindHost.setToolTipText("Địa chỉ IP LAN của máy bạn. Gửi IP này cho bạn bè để họ nhập vào Client kết nối tới.");

        btnStart = UITheme.createSuccessButton("Khởi động Server");
        btnStart.addActionListener(e -> startServerAction());
        
        btnStop = UITheme.createDangerButton("Dừng Server");
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> stopServerAction());
        
        controlCard.add(createLabeledField("Cổng Port", txtPort));
        controlCard.add(createLabeledField("Địa chỉ bind", txtBindHost));
        controlCard.add(Box.createHorizontalStrut(10));
        controlCard.add(btnStart);
        controlCard.add(btnStop);

        // Metrics Panel
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);
        
        lblClientCount = new JLabel("0");
        lblMessageCount = new JLabel("0");
        lblUptime = new JLabel("00:00:00");
        lblErrorCount = new JLabel("0");
        
        metricsPanel.add(createMetricCard("Client đang kết nối", lblClientCount, "/ 50 tối đa"));
        metricsPanel.add(createMetricCard("Tin nhắn đã chuyển", lblMessageCount, "trong phiên"));
        metricsPanel.add(createMetricCard("Thời gian hoạt động", lblUptime, ""));
        metricsPanel.add(createMetricCard("Lỗi kết nối", lblErrorCount, "không có"));

        JPanel topCenterPanel = new JPanel(new BorderLayout(0, 15));
        topCenterPanel.setOpaque(false);
        topCenterPanel.add(controlCard, BorderLayout.NORTH);
        topCenterPanel.add(metricsPanel, BorderLayout.CENTER);
        
        centerPanel.add(topCenterPanel, BorderLayout.NORTH);

        // Split Pane (Console and Table)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Console Panel
        JPanel consoleCard = UITheme.createCard();
        consoleCard.setLayout(new BorderLayout(0, 10));
        
        JPanel consoleHeader = new JPanel(new BorderLayout());
        consoleHeader.setOpaque(false);
        JLabel lblConsoleTitle = new JLabel("Nhật ký TCP Server");
        lblConsoleTitle.setFont(UITheme.FONT_HEADER);
        chkAutoScroll = new JCheckBox("Auto-scroll", true);
        chkAutoScroll.setOpaque(false);
        consoleHeader.add(lblConsoleTitle, BorderLayout.WEST);
        consoleHeader.add(chkAutoScroll, BorderLayout.EAST);
        
        consolePane = new JTextPane();
        consolePane.setEditable(false);
        consolePane.setBackground(UITheme.CONSOLE_BG);
        consolePane.setFont(UITheme.FONT_MONO);
        consolePane.setBorder(new EmptyBorder(10, 10, 10, 10));
        consoleDoc = consolePane.getStyledDocument();
        
        JScrollPane scrollConsole = new JScrollPane(consolePane);
        scrollConsole.setBorder(null);
        
        consoleCard.add(consoleHeader, BorderLayout.NORTH);
        consoleCard.add(scrollConsole, BorderLayout.CENTER);
        
        // Table Panel
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        
        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel lblTableTitle = new JLabel("Danh sách Client đang kết nối");
        lblTableTitle.setFont(UITheme.FONT_HEADER);
        tableHeader.add(lblTableTitle, BorderLayout.WEST);
        
        JButton btnKickAll = UITheme.createDangerButton("Ngắt tất cả");
        btnKickAll.addActionListener(e -> {
            chatServerBUS.broadcast(ChatMessage.system("Server đang ngắt toàn bộ kết nối!"));
            chatServerBUS.stopServer();
        });
        tableHeader.add(btnKickAll, BorderLayout.EAST);
        
        String[] cols = {"TÊN / CLIENT ID", "ĐỊA CHỈ IP", "CỔNG", "TRẠNG THÁI"};
        clientTableModel = new DefaultTableModel(cols, 0);
        tblClients = new JTable(clientTableModel);
        tblClients.setRowHeight(36);
        tblClients.setFont(UITheme.FONT_REGULAR);
        tblClients.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblClients.getTableHeader().setFont(UITheme.FONT_BOLD);
        
        JScrollPane scrollTable = new JScrollPane(tblClients);
        scrollTable.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true));
        
        tableCard.add(tableHeader, BorderLayout.NORTH);
        tableCard.add(scrollTable, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(consoleCard);
        splitPane.setRightComponent(tableCard);
        
        centerPanel.add(splitPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
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

    public void startServerAction() {
        if (chatServerBUS.isRunning()) return;
        try {
            int port = Integer.parseInt(txtPort.getText().trim());
            chatServerBUS.startServer(port);
            
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
            txtPort.setEnabled(false);
            txtBindHost.setEnabled(false);
            
            lblStatusBadge.setText("Đang chạy");
            lblStatusBadge.setBackground(UITheme.SUCCESS_BG);
            lblStatusBadge.setForeground(UITheme.SUCCESS);
            
            startTime = System.currentTimeMillis();
            startUptimeTimer();
            errorCount = 0;
            lblErrorCount.setText("0");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Port phải là số nguyên hợp lệ!");
        }
    }

    private void stopServerAction() {
        chatServerBUS.stopServer();
        btnStart.setEnabled(true);
        btnStop.setEnabled(false);
        txtPort.setEnabled(true);
        txtBindHost.setEnabled(true);
        
        lblStatusBadge.setText("Đã dừng");
        lblStatusBadge.setBackground(UITheme.BORDER_COLOR);
        lblStatusBadge.setForeground(UITheme.TEXT_MUTED);
        
        stopUptimeTimer();
    }



    private void appendLog(String prefix, String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            try {
                String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                StyleContext sc = StyleContext.getDefaultStyleContext();
                
                AttributeSet timeStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, UITheme.TEXT_MUTED);
                consoleDoc.insertString(consoleDoc.getLength(), time + "  ", timeStyle);
                
                AttributeSet prefixStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
                prefixStyle = sc.addAttribute(prefixStyle, StyleConstants.Bold, true);
                consoleDoc.insertString(consoleDoc.getLength(), String.format("%-6s", prefix), prefixStyle);
                
                AttributeSet textStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, UITheme.CONSOLE_FG);
                consoleDoc.insertString(consoleDoc.getLength(), " " + message + "\n", textStyle);
                
                if (chkAutoScroll.isSelected()) {
                    consolePane.setCaretPosition(consoleDoc.getLength());
                }
            } catch (Exception ignored) {}
        });
    }

    @Override
    public void onServerStarted(int port) {
        appendLog("INFO", "TCP Server đang lắng nghe tại " + localIp + ":" + port, UITheme.PRIMARY);
        appendLog("INFO", "=> Hãy bảo bạn bè nhập IP: " + localIp + " để kết nối!", UITheme.SUCCESS);
    }

    @Override
    public void onServerStopped() {
        appendLog("INFO", "TCP Server đã dừng.", UITheme.TEXT_MUTED);
        SwingUtilities.invokeLater(() -> {
            clientTableModel.setRowCount(0);
            lblClientCount.setText("0");
        });
    }

    @Override
    public void onLog(String logMessage) {
        appendLog("LOG", logMessage, UITheme.CONSOLE_FG);
        SwingUtilities.invokeLater(() -> {
            int msgCount = chatServerBUS.getHistoryDAO().getHistory().size();
            lblMessageCount.setText(String.valueOf(msgCount));
        });
    }

    @Override
    public void onClientListChanged(List<User> activeUsers) {
        SwingUtilities.invokeLater(() -> {
            lblClientCount.setText(String.valueOf(activeUsers.size()));
            clientTableModel.setRowCount(0);
            for (User u : activeUsers) {
                clientTableModel.addRow(new Object[]{
                        "● " + u.getUsername(), u.getIpAddress(), u.getPort(), "● Online"
                });
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        appendLog("ERROR", errorMessage, UITheme.DANGER);
        SwingUtilities.invokeLater(() -> {
            errorCount++;
            lblErrorCount.setText(String.valueOf(errorCount));
            if (errorCount > 0) lblErrorCount.setForeground(UITheme.DANGER);
        });
    }

    private void startUptimeTimer() {
        if (uptimeTimer != null) uptimeTimer.cancel();
        uptimeTimer = new Timer(true);
        uptimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long diff = System.currentTimeMillis() - startTime;
                long s = diff / 1000 % 60;
                long m = diff / (60 * 1000) % 60;
                long h = diff / (60 * 60 * 1000);
                SwingUtilities.invokeLater(() -> lblUptime.setText(String.format("%02d:%02d:%02d", h, m, s)));
            }
        }, 0, 1000);
    }

    private void stopUptimeTimer() {
        if (uptimeTimer != null) uptimeTimer.cancel();
    }
}
