package com.ltudm.gui.chat;

import com.ltudm.bus.chat.ChatClientBUS;
import com.ltudm.dto.ChatMessage;
import com.ltudm.dto.MessageType;
import com.ltudm.gui.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class TcpClientPanel extends JPanel implements ChatClientBUS.ClientEventListener {
    private final ChatClientBUS clientBUS;

    private JLabel lblStatusIndicator;
    private JButton btnConnect;
    
    private JTextField txtUsername;
    private JTextField txtHost;
    private JTextField txtPort;

    private JTextPane chatPane;
    private StyledDocument doc;

    private JComboBox<String> cbRecipient;
    private JTextField txtInput;
    private JButton btnSend;

    private DefaultListModel<String> onlineListModel;
    private JList<String> listOnlineUsers;
    private JLabel lblOnlineHeader;

    private String myUsername = "";

    public TcpClientPanel() {
        this.clientBUS = new ChatClientBUS();
        this.clientBUS.setListener(this);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Phòng chat nội bộ");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        
        lblStatusIndicator = UITheme.createBadge("Chưa kết nối", UITheme.BORDER_COLOR, UITheme.TEXT_MUTED);
        
        titlePanel.add(lblTitle);
        titlePanel.add(lblStatusIndicator);
        
        JLabel lblDesc = new JLabel("Kết nối tới TCP Chat Server, theo dõi người dùng trực tuyến và trao đổi tin nhắn.");
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setBorder(new EmptyBorder(5, 10, 0, 0));
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);
        
        // Right header actions (Disconnect)
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightHeader.setOpaque(false);
        JButton btnDisconnect = UITheme.createDangerButton("Ngắt kết nối");
        btnDisconnect.addActionListener(e -> clientBUS.disconnect());
        rightHeader.add(btnDisconnect);
        headerPanel.add(rightHeader, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Split Main Content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);
        splitPane.setDividerSize(10);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        
        // --- LEFT PANEL ---
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setOpaque(false);
        
        // Connection Card
        JPanel connCard = UITheme.createCard();
        connCard.setLayout(new BoxLayout(connCard, BoxLayout.Y_AXIS));
        
        JLabel lblConnTitle = new JLabel("Kết nối phòng chat");
        lblConnTitle.setFont(UITheme.FONT_HEADER);
        lblConnTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel lblConnDesc = new JLabel("Thông tin định danh và máy chủ đích.");
        lblConnDesc.setFont(UITheme.FONT_SMALL);
        lblConnDesc.setForeground(UITheme.TEXT_MUTED);
        lblConnDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        connCard.add(lblConnTitle);
        connCard.add(lblConnDesc);
        connCard.add(Box.createVerticalStrut(15));
        
        txtUsername = new JTextField("nguyen.an");
        txtUsername.setBorder(UITheme.createFieldBorder());
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFormGroup(connCard, "Tên người dùng", txtUsername);
        
        JPanel hostPortPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        hostPortPanel.setOpaque(false);
        hostPortPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hostPortPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        txtHost = new JTextField("");
        txtHost.setBorder(UITheme.createFieldBorder());
        txtHost.setToolTipText("Nhập địa chỉ IP của máy chạy Server (ví dụ: 192.168.1.5). Nếu chạy trên cùng máy, nhập localhost.");
        
        txtPort = new JTextField("8888");
        txtPort.setBorder(UITheme.createFieldBorder());
        
        JPanel pnlHost = new JPanel(new BorderLayout()); pnlHost.setOpaque(false);
        JLabel l1 = new JLabel("Địa chỉ Server"); l1.setFont(UITheme.FONT_SMALL); l1.setForeground(UITheme.TEXT_MUTED);
        pnlHost.add(l1, BorderLayout.NORTH); pnlHost.add(txtHost, BorderLayout.CENTER);
        
        JPanel pnlPort = new JPanel(new BorderLayout()); pnlPort.setOpaque(false);
        JLabel l2 = new JLabel("Cổng Port"); l2.setFont(UITheme.FONT_SMALL); l2.setForeground(UITheme.TEXT_MUTED);
        pnlPort.add(l2, BorderLayout.NORTH); pnlPort.add(txtPort, BorderLayout.CENTER);
        
        hostPortPanel.add(pnlHost);
        hostPortPanel.add(pnlPort);
        
        connCard.add(hostPortPanel);
        connCard.add(Box.createVerticalStrut(15));
        
        btnConnect = UITheme.createSuccessButton("Kết nối tới Server");
        btnConnect.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnConnect.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnConnect.addActionListener(e -> {
            clientBUS.connect(txtHost.getText(), Integer.parseInt(txtPort.getText()), txtUsername.getText());
        });
        connCard.add(btnConnect);
        
        leftPanel.add(connCard, BorderLayout.NORTH);
        
        // Online Card
        JPanel onlineCard = UITheme.createCard();
        onlineCard.setLayout(new BorderLayout(0, 10));
        lblOnlineHeader = new JLabel("Online (0)");
        lblOnlineHeader.setFont(UITheme.FONT_HEADER);
        onlineCard.add(lblOnlineHeader, BorderLayout.NORTH);
        
        onlineListModel = new DefaultListModel<>();
        listOnlineUsers = new JList<>(onlineListModel);
        listOnlineUsers.setFont(UITheme.FONT_REGULAR);
        listOnlineUsers.setFixedCellHeight(32);
        listOnlineUsers.setBorder(null);
        
        listOnlineUsers.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                pnl.setBackground(isSelected ? UITheme.BG_MAIN : UITheme.CARD_BG);
                
                String name = (String) value;
                JLabel avatar = new JLabel(name.substring(0, 1).toUpperCase(), SwingConstants.CENTER);
                avatar.setOpaque(true);
                avatar.setBackground(UITheme.PRIMARY);
                avatar.setForeground(Color.WHITE);
                avatar.setPreferredSize(new Dimension(24, 24));
                avatar.setFont(UITheme.FONT_BOLD);
                
                JLabel lblName = new JLabel(name.equalsIgnoreCase(myUsername) ? name + " (Bạn)" : name);
                lblName.setFont(UITheme.FONT_REGULAR);
                
                JLabel status = new JLabel("●");
                status.setForeground(UITheme.SUCCESS);
                status.setFont(new Font("Arial", Font.BOLD, 10));
                
                pnl.add(avatar);
                pnl.add(lblName);
                pnl.add(status);
                
                return pnl;
            }
        });
        
        listOnlineUsers.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = listOnlineUsers.getSelectedValue();
                if (selected != null && !selected.equalsIgnoreCase(myUsername)) {
                    cbRecipient.setSelectedItem(selected);
                }
            }
        });
        
        JScrollPane scrollOnline = new JScrollPane(listOnlineUsers);
        scrollOnline.setBorder(null);
        onlineCard.add(scrollOnline, BorderLayout.CENTER);
        
        leftPanel.add(onlineCard, BorderLayout.CENTER);
        
        // --- RIGHT PANEL (Chat Area) ---
        JPanel rightPanel = UITheme.createCard();
        rightPanel.setLayout(new BorderLayout(0, 10));
        
        JPanel chatHeader = new JPanel(new BorderLayout());
        chatHeader.setOpaque(false);
        JLabel lblChatTitle = new JLabel("Chat tất cả");
        lblChatTitle.setFont(UITheme.FONT_HEADER);
        chatHeader.add(lblChatTitle, BorderLayout.WEST);
        
        cbRecipient = new JComboBox<>();
        cbRecipient.addItem("Tất cả mọi người");
        chatHeader.add(cbRecipient, BorderLayout.EAST);
        
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(UITheme.FONT_REGULAR);
        chatPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        doc = chatPane.getStyledDocument();
        
        JScrollPane scrollChat = new JScrollPane(chatPane);
        scrollChat.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true));
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        
        txtInput = new JTextField();
        txtInput.setBorder(UITheme.createFieldBorder());
        txtInput.setPreferredSize(new Dimension(0, 40));
        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessageAction();
                }
            }
        });
        
        btnSend = UITheme.createPrimaryButton("Gửi");
        btnSend.addActionListener(e -> sendMessageAction());
        
        inputPanel.add(txtInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        
        rightPanel.add(chatHeader, BorderLayout.NORTH);
        rightPanel.add(scrollChat, BorderLayout.CENTER);
        rightPanel.add(inputPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void addFormGroup(JPanel parent, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(lbl);
        parent.add(Box.createVerticalStrut(4));
        parent.add(field);
        parent.add(Box.createVerticalStrut(10));
    }

    private void sendMessageAction() {
        String text = txtInput.getText().trim();
        if (text.isEmpty()) return;

        if (!clientBUS.isConnected()) {
            JOptionPane.showMessageDialog(this, "Bạn chưa kết nối tới Server!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedIndex = cbRecipient.getSelectedIndex();
        if (selectedIndex <= 0) {
            clientBUS.sendPublicMessage(text);
        } else {
            String recipient = (String) cbRecipient.getSelectedItem();
            clientBUS.sendPrivateMessage(recipient, text);
        }

        txtInput.setText("");
        txtInput.requestFocus();
    }

    private void appendMessage(String time, String sender, String content, Color color, boolean isBold) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyleContext sc = StyleContext.getDefaultStyleContext();
                AttributeSet timeStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, UITheme.TEXT_MUTED);
                doc.insertString(doc.getLength(), time + " ", timeStyle);

                AttributeSet senderStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
                senderStyle = sc.addAttribute(senderStyle, StyleConstants.Bold, isBold);
                doc.insertString(doc.getLength(), "[" + sender + "]: ", senderStyle);

                AttributeSet contentStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, UITheme.TEXT_DARK);
                doc.insertString(doc.getLength(), content + "\n", contentStyle);

                chatPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    private void appendSystemMessage(String time, String content) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyleContext sc = StyleContext.getDefaultStyleContext();
                AttributeSet sysStyle = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, UITheme.TEXT_MUTED);
                sysStyle = sc.addAttribute(sysStyle, StyleConstants.Italic, true);
                doc.insertString(doc.getLength(), "["+time+"] " + content + "\n", sysStyle);
                chatPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    @Override
    public void onConnected(String assignedName, String host, int port) {
        SwingUtilities.invokeLater(() -> {
            this.myUsername = assignedName;
            lblStatusIndicator.setText("Đã kết nối");
            lblStatusIndicator.setBackground(UITheme.SUCCESS_BG);
            lblStatusIndicator.setForeground(UITheme.SUCCESS);
            appendSystemMessage("", "Đã tham gia phòng chat dưới tên " + assignedName);
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            lblStatusIndicator.setText("Chưa kết nối");
            lblStatusIndicator.setBackground(UITheme.BORDER_COLOR);
            lblStatusIndicator.setForeground(UITheme.TEXT_MUTED);
            onlineListModel.clear();
            lblOnlineHeader.setText("Online (0)");
            cbRecipient.removeAllItems();
            cbRecipient.addItem("Tất cả mọi người");
            appendSystemMessage("", "Đã ngắt kết nối khỏi phòng chat.");
        });
    }

    @Override
    public void onMessageReceived(ChatMessage message) {
        if (message == null) return;
        MessageType type = message.getType();
        String time = message.getTimestamp();
        String sender = message.getSender();
        String content = message.getContent();

        if (type == MessageType.PUBLIC) {
            boolean isMe = sender.equalsIgnoreCase(myUsername);
            Color senderColor = isMe ? UITheme.PRIMARY : UITheme.TEXT_DARK;
            appendMessage(time, sender, content, senderColor, true);
        } else if (type == MessageType.PRIVATE) {
            String recipient = message.getRecipient();
            if (sender.equalsIgnoreCase(myUsername)) {
                appendMessage(time, "Tới " + recipient, content, UITheme.PURPLE, true);
            } else {
                appendMessage(time, "Từ " + sender, content, UITheme.PURPLE, true);
            }
        } else if (type == MessageType.SYSTEM) {
            appendSystemMessage(time, content);
        }
    }

    @Override
    public void onUserListUpdated(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            onlineListModel.clear();
            for (String u : users) {
                onlineListModel.addElement(u);
            }
            lblOnlineHeader.setText("Online (" + users.size() + ")");

            String currentSelected = (String) cbRecipient.getSelectedItem();
            cbRecipient.removeAllItems();
            cbRecipient.addItem("Tất cả mọi người");
            for (String u : users) {
                if (!u.equalsIgnoreCase(myUsername)) {
                    cbRecipient.addItem(u);
                }
            }
            if (currentSelected != null && !currentSelected.equals("Tất cả mọi người")) {
                cbRecipient.setSelectedItem(currentSelected);
            }
        });
    }

    @Override
    public void onError(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, errorMessage, "Lỗi", JOptionPane.ERROR_MESSAGE);
        });
    }
}
