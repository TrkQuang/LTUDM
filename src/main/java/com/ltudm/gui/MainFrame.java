package com.ltudm.gui;

import com.ltudm.bus.chat.ChatServerBUS;
import com.ltudm.bus.http.HttpServerBUS;
import com.ltudm.gui.chat.TcpServerPanel;
import com.ltudm.gui.http.HttpDashboardPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final ChatServerBUS chatServerBUS;
    private final HttpServerBUS httpServerBUS;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private Map<String, JPanel> sidebarItems = new HashMap<>();

    public MainFrame() {
        this.chatServerBUS = new ChatServerBUS();
        this.httpServerBUS = new HttpServerBUS();
        this.httpServerBUS.setChatServerBUS(this.chatServerBUS);

        initUI();
    }

    private void initUI() {
        setTitle("LTUDM Console");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_MAIN);

        // Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 0));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 25));
        logoPanel.setOpaque(false);
        JLabel lblLogo = new JLabel("LT");
        lblLogo.setOpaque(true);
        lblLogo.setBackground(UITheme.PRIMARY);
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Inter", Font.BOLD, 18));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblLogo.setPreferredSize(new Dimension(40, 40));
        
        JLabel lblTitle = new JLabel("<html><b style='font-size:14px;color:white;'>LTUDM Console</b><br/><span style='font-size:10px;color:#94a3b8;'>Lập trình ứng dụng mạng</span></html>");
        logoPanel.add(lblLogo);
        logoPanel.add(lblTitle);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Menu Items
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel lblHeader1 = new JLabel("KHÔNG GIAN THỰC HÀNH");
        lblHeader1.setForeground(UITheme.SIDEBAR_TEXT);
        lblHeader1.setFont(new Font("Inter", Font.BOLD, 10));
        lblHeader1.setBorder(new EmptyBorder(10, 5, 10, 5));
        menuPanel.add(lblHeader1);

        // Main Content Area
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(UITheme.BG_MAIN);

        TcpServerPanel tcpServerPanel = new TcpServerPanel(chatServerBUS);
        
        HttpDashboardPanel httpTab = new HttpDashboardPanel(httpServerBUS);

        DataManagementPanel dataManagementPanel = new DataManagementPanel(httpServerBUS.getStudentDAO(), httpServerBUS.getMessageDAO());

        mainContentPanel.add(tcpServerPanel, "TcpServer");
        mainContentPanel.add(httpTab, "HttpServer");
        mainContentPanel.add(dataManagementPanel, "Data");

        // Menu actions
        addMenuItem(menuPanel, "TCP Chat Server", "Quản lý kết nối", "TcpServer", true);
        addMenuItem(menuPanel, "HTTP Server", "Cấu hình & Giám sát", "HttpServer", false);
        addMenuItem(menuPanel, "Quản lý tầng DAO", "GUI - BUS - DAO", "Data", false);

        // Dummy items removed by user request
        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Footer Sidebar
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        footerPanel.setOpaque(false);
        JLabel lblStatus = new JLabel("<html><b style='color:#4ade80;'>●</b> Môi trường sẵn sàng<br/><span style='font-size:10px;color:#94a3b8;'>Java 17 - Terminal</span></html>");
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setFont(UITheme.FONT_REGULAR);
        footerPanel.add(lblStatus);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        rootPanel.add(sidebar, BorderLayout.WEST);
        rootPanel.add(mainContentPanel, BorderLayout.CENTER);
        
        setContentPane(rootPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                chatServerBUS.stopServer();
                httpServerBUS.stop();
            }
        });
    }

    private void addMenuItem(JPanel container, String title, String subtitle, String cardName, boolean active) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(8, 10, 8, 10));
        item.setMaximumSize(new Dimension(300, 50));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblItem = new JLabel("<html><b>" + title + "</b><br/><span style='font-size:10px;'>" + subtitle + "</span></html>");
        lblItem.setForeground(active ? UITheme.SIDEBAR_TEXT_ACTIVE : UITheme.SIDEBAR_TEXT);
        item.add(lblItem, BorderLayout.CENTER);

        if (active) {
            item.setOpaque(true);
            item.setBackground(UITheme.SIDEBAR_ACTIVE);
        }

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (cardName != null) {
                    cardLayout.show(mainContentPanel, cardName);
                    
                    // Update active styling
                    for (JPanel p : sidebarItems.values()) {
                        p.setOpaque(false);
                        p.setBackground(UITheme.BG_SIDEBAR);
                        ((JLabel)p.getComponent(0)).setForeground(UITheme.SIDEBAR_TEXT);
                        p.repaint();
                    }
                    item.setOpaque(true);
                    item.setBackground(UITheme.SIDEBAR_ACTIVE);
                    lblItem.setForeground(UITheme.SIDEBAR_TEXT_ACTIVE);
                    item.repaint();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!item.isOpaque()) {
                    item.setOpaque(true);
                    item.setBackground(UITheme.SIDEBAR_HOVER);
                    item.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (item.getBackground().equals(UITheme.SIDEBAR_HOVER)) {
                    item.setOpaque(false);
                    item.repaint();
                }
            }
        });

        sidebarItems.put(title, item);
        container.add(item);
        container.add(Box.createVerticalStrut(5));
    }

    public static void main(String[] args) {
        UITheme.setupFlatLaf();
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
