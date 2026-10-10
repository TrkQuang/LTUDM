package com.ltudm.gui;

import com.ltudm.dao.MessageDAO;
import com.ltudm.dao.StudentDAO;
import com.ltudm.dto.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DataManagementPanel extends JPanel {
    private final StudentDAO studentDAO;
    private final MessageDAO messageDAO;

    private JTable tblRecords;
    private DefaultTableModel tableModel;

    // Side form fields
    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtMajor;
    private JComboBox<String> cbStatus;

    // Metrics labels
    private JLabel lblTotalRecords = new JLabel("0");
    private JLabel lblTotalSub = new JLabel("người dùng");
    private JLabel lblActiveRecords = new JLabel("0");
    private JLabel lblActiveSub = new JLabel("0%");
    private JLabel lblLockedRecords = new JLabel("0");
    private JLabel lblLockedSub = new JLabel("0%");
    private JLabel lblTblSub;

    public DataManagementPanel(StudentDAO studentDAO, MessageDAO messageDAO) {
        this.studentDAO = studentDAO;
        this.messageDAO = messageDAO;
        initUI();
        loadDummyDataIfEmpty();
    }
    
    private void loadDummyDataIfEmpty() {
        reloadStudents();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("Quản lý dữ liệu tầng DAO");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        titlePanel.add(lblTitle);
        
        JLabel lblDesc = new JLabel("Thao tác CRUD qua mô hình phân lớp, kiểm tra tính hợp lệ tại BUS và theo dõi truy vấn tại DAO.");
        lblDesc.setForeground(UITheme.TEXT_MUTED);
        lblDesc.setFont(UITheme.FONT_REGULAR);
        lblDesc.setBorder(new EmptyBorder(5, 10, 0, 0));
        
        headerPanel.add(titlePanel, BorderLayout.NORTH);
        headerPanel.add(lblDesc, BorderLayout.SOUTH);
        
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerActions.setOpaque(false);
        JButton btnImport = UITheme.createOutlineButton("Nhập CSV");
        btnImport.addActionListener(e -> importCsv());
        headerActions.add(btnImport);
        
        JButton btnExport = UITheme.createOutlineButton("Xuất dữ liệu");
        btnExport.addActionListener(e -> exportCsv());
        headerActions.add(btnExport);
        JButton btnAdd = UITheme.createPrimaryButton("+ Thêm bản ghi");
        btnAdd.addActionListener(e -> showAddDialog());
        headerActions.add(btnAdd);
        headerPanel.add(headerActions, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content
        JPanel centerContainer = new JPanel(new BorderLayout(0, 15));
        centerContainer.setOpaque(false);

        // Metrics
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        metricsPanel.setOpaque(false);
        metricsPanel.add(createMetricCard("Tổng bản ghi", lblTotalRecords, lblTotalSub, UITheme.PRIMARY));
        metricsPanel.add(createMetricCard("Đang hoạt động", lblActiveRecords, lblActiveSub, UITheme.SUCCESS));
        metricsPanel.add(createMetricCard("Tạm khóa", lblLockedRecords, lblLockedSub, UITheme.WARNING));
        JLabel lblQueries = new JLabel("342");
        JLabel lblQueriesSub = new JLabel("0 lỗi");
        metricsPanel.add(createMetricCard("Truy vấn hôm nay", lblQueries, lblQueriesSub, UITheme.PURPLE));
        centerContainer.add(metricsPanel, BorderLayout.NORTH);

        // Main Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerSize(15);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        // Left: Table
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 15));
        
        JPanel tblHeader = new JPanel(new BorderLayout());
        tblHeader.setOpaque(false);
        JLabel lblTblTitle = new JLabel("Danh sách người dùng");
        lblTblTitle.setFont(UITheme.FONT_HEADER);
        lblTblSub = new JLabel("Bảng USER - 0 bản ghi");
        lblTblSub.setFont(UITheme.FONT_SMALL);
        lblTblSub.setForeground(UITheme.TEXT_MUTED);
        
        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.add(lblTblTitle, BorderLayout.NORTH);
        titleWrap.add(lblTblSub, BorderLayout.SOUTH);
        tblHeader.add(titleWrap, BorderLayout.WEST);
        
        JLabel daoSync = UITheme.createBadge("● DAO synchronized", UITheme.SUCCESS_BG, UITheme.SUCCESS);
        tblHeader.add(daoSync, BorderLayout.EAST);
        
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        JTextField txtSearch = new JTextField("Tìm theo họ tên, email hoặc mã...");
        txtSearch.setPreferredSize(new Dimension(250, 34));
        txtSearch.setBorder(UITheme.createFieldBorder());
        txtSearch.setForeground(UITheme.TEXT_MUTED);
        filterBar.add(txtSearch);
        filterBar.add(UITheme.createOutlineButton("Mọi vai trò"));
        filterBar.add(UITheme.createOutlineButton("Mọi trạng thái"));
        JButton btnRefresh = UITheme.createOutlineButton("Làm mới");
        btnRefresh.addActionListener(e -> reloadStudents());
        filterBar.add(btnRefresh);
        
        JPanel topTable = new JPanel(new BorderLayout(0, 10));
        topTable.setOpaque(false);
        topTable.add(tblHeader, BorderLayout.NORTH);
        topTable.add(filterBar, BorderLayout.CENTER);
        
        tableCard.add(topTable, BorderLayout.NORTH);
        
        String[] cols = {"ID", "HỌ TÊN / EMAIL", "VAI TRÒ", "TRẠNG THÁI", "CẬP NHẬT", "TÁC VỤ"};
        tableModel = new DefaultTableModel(cols, 0);
        tblRecords = new JTable(tableModel);
        tblRecords.setRowHeight(50);
        tblRecords.setIntercellSpacing(new Dimension(0, 0));
        tblRecords.setShowVerticalLines(false);
        tblRecords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        tblRecords.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if ("● Hoạt động".equals(value)) {
                    l.setForeground(UITheme.SUCCESS);
                } else {
                    l.setForeground(UITheme.WARNING);
                }
                l.setFont(UITheme.FONT_BOLD);
                return l;
            }
        });
        
        tblRecords.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblRecords.getSelectedRow() != -1) {
                int r = tblRecords.getSelectedRow();
                String id = tblRecords.getValueAt(r, 0).toString();
                Student s = studentDAO.getAll().stream().filter(st -> st.getId().equals(id)).findFirst().orElse(null);
                if (s != null) {
                    txtName.setText(s.getName());
                    txtEmail.setText(s.getEmail());
                    txtMajor.setText(s.getMajor());
                    cbStatus.setSelectedIndex(s.getGpa() >= 2.0 ? 0 : 1);
                }
            }
        });
        
        JScrollPane scrollTable = new JScrollPane(tblRecords);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        tableCard.add(scrollTable, BorderLayout.CENTER);
        
        splitPane.setLeftComponent(tableCard);
        
        // Right: Detail Form
        JPanel rightCol = new JPanel(new BorderLayout(0, 15));
        rightCol.setOpaque(false);
        
        JPanel detailCard = UITheme.createCard();
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));
        
        JPanel detailHeader = new JPanel(new BorderLayout());
        detailHeader.setOpaque(false);
        JLabel lblDetTitle = new JLabel("Chi tiết bản ghi");
        lblDetTitle.setFont(UITheme.FONT_HEADER);
        detailHeader.add(lblDetTitle, BorderLayout.WEST);
        detailCard.add(detailHeader);
        detailCard.add(Box.createVerticalStrut(15));
        
        txtName = new JTextField(); txtName.setBorder(UITheme.createFieldBorder());
        txtEmail = new JTextField(); txtEmail.setBorder(UITheme.createFieldBorder());
        txtMajor = new JTextField(); txtMajor.setBorder(UITheme.createFieldBorder());
        cbStatus = new JComboBox<>(new String[]{"Hoạt động", "Tạm khóa"});
        
        addFormGroup(detailCard, "Họ và tên", txtName);
        addFormGroup(detailCard, "Email", txtEmail);
        addFormGroup(detailCard, "Vai trò", txtMajor);
        
        JPanel statusPnl = new JPanel(new BorderLayout(0, 4));
        statusPnl.setOpaque(false);
        JLabel lblSts = new JLabel("Trạng thái"); lblSts.setFont(UITheme.FONT_SMALL); lblSts.setForeground(UITheme.TEXT_MUTED);
        statusPnl.add(lblSts, BorderLayout.NORTH);
        statusPnl.add(cbStatus, BorderLayout.CENTER);
        detailCard.add(statusPnl);
        detailCard.add(Box.createVerticalStrut(20));
        
        JPanel actionPnl = new JPanel(new GridLayout(1, 2, 10, 0));
        actionPnl.setOpaque(false);
        JButton btnDel = UITheme.createDangerButton("Xóa");
        btnDel.addActionListener(e -> {
            int r = tblRecords.getSelectedRow();
            if (r >= 0) {
                studentDAO.deleteStudent(tblRecords.getValueAt(r, 0).toString());
                reloadStudents();
            }
        });
        JButton btnSave = UITheme.createPrimaryButton("Lưu thay đổi");
        btnSave.addActionListener(e -> {
            int r = tblRecords.getSelectedRow();
            if (r >= 0) {
                String id = tblRecords.getValueAt(r, 0).toString();
                double gpa = cbStatus.getSelectedIndex() == 0 ? 4.0 : 1.5;
                studentDAO.updateStudent(new Student(id, txtName.getText(), txtMajor.getText(), gpa, txtEmail.getText()));
                reloadStudents();
            }
        });
        actionPnl.add(btnDel);
        actionPnl.add(btnSave);
        detailCard.add(actionPnl);
        
        rightCol.add(detailCard, BorderLayout.NORTH);
        
        JPanel logCard = UITheme.createCard();
        logCard.setLayout(new BorderLayout(0, 10));
        JLabel lblLog = new JLabel("Luồng xử lý gần nhất");
        lblLog.setFont(UITheme.FONT_HEADER);
        logCard.add(lblLog, BorderLayout.NORTH);
        
        JTextArea txtLogs = new JTextArea("GUI\nNhận sự kiện cập nhật\n\nBUS\nKiểm tra dữ liệu hợp lệ\n\nDAO\nUPDATE users SET...");
        txtLogs.setEditable(false);
        txtLogs.setFont(UITheme.FONT_SMALL);
        txtLogs.setForeground(UITheme.TEXT_MUTED);
        txtLogs.setBorder(null);
        logCard.add(txtLogs, BorderLayout.CENTER);
        
        rightCol.add(logCard, BorderLayout.CENTER);
        
        splitPane.setRightComponent(rightCol);
        
        centerContainer.add(splitPane, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }
    
    private void addFormGroup(JPanel parent, String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(field, BorderLayout.CENTER);
        parent.add(p);
        parent.add(Box.createVerticalStrut(10));
    }
    
    private JPanel createMetricCard(String title, JLabel lblVal, JLabel lblSub, Color c) {
        JPanel card = UITheme.createCard();
        card.setLayout(new BorderLayout());
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);
        
        lblVal.setFont(new Font("Inter", Font.BOLD, 28));
        lblVal.setForeground(UITheme.TEXT_DARK);
        

        lblSub.setBorder(new EmptyBorder(10, 5, 0, 0));
        
        JPanel vPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        vPnl.setOpaque(false);
        vPnl.add(lblVal);
        vPnl.add(lblSub);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(vPnl, BorderLayout.CENTER);
        return card;
    }
    
    public void reloadStudents() {
        tableModel.setRowCount(0);
        List<Student> list = studentDAO.getAll();
        
        int activeCount = 0;
        int lockedCount = 0;
        
        for (Student s : list) {
            boolean isActive = s.getGpa() >= 2.0;
            if (isActive) activeCount++;
            else lockedCount++;
            
            String status = isActive ? "● Hoạt động" : "● Tạm khóa";
            tableModel.addRow(new Object[]{
                    s.getId(),
                    "<html><b>" + s.getName() + "</b><br><span style='color:#64748b;font-size:9px;'>" + s.getEmail() + "</span></html>",
                    s.getMajor(),
                    status,
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()),
                    "✎"
            });
        }
        
        int total = list.size();
        lblTotalRecords.setText(String.valueOf(total));
        lblActiveRecords.setText(String.valueOf(activeCount));
        lblLockedRecords.setText(String.valueOf(lockedCount));
        
        if (total > 0) {
            lblActiveSub.setText(String.format("%.1f%%", (activeCount * 100.0) / total));
            lblLockedSub.setText(String.format("%.1f%%", (lockedCount * 100.0) / total));
        } else {
            lblActiveSub.setText("0%");
            lblLockedSub.setText("0%");
        }
        
        if (lblTblSub != null) {
            String time = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
            lblTblSub.setText(String.format("Bảng USER - %d bản ghi - cập nhật gần nhất %s", total, time));
        }
    }
    
    public void reloadMessages() {
        // Ignored for UI demo mapping
    }
    
    private void showAddDialog() {
        studentDAO.addStudent(new Student("USR-00" + (studentDAO.getAll().size() + 1), "Tên mới", "Vai trò mới", 4.0, "email@ltudm.vn"));
        reloadStudents();
    }
    
    private void importCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn file CSV để nhập");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                String line;
                boolean isFirstLine = true;
                int imported = 0;
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) { isFirstLine = false; continue; } // Bỏ qua header
                    String[] data = line.split(",");
                    if (data.length >= 5) {
                        try {
                            double gpa = Double.parseDouble(data[3].trim());
                            Student s = new Student(data[0].trim(), data[1].trim(), data[2].trim(), gpa, data[4].trim());
                            // Add student, ignoring if ID already exists (handled by DAO)
                            if (studentDAO.addStudent(s)) {
                                imported++;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                reloadStudents();
                JOptionPane.showMessageDialog(this, "Đã nhập thành công " + imported + " bản ghi!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi đọc file CSV: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn nơi lưu file CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new java.io.File(file.getParentFile(), file.getName() + ".csv");
            }
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(file))) {
                pw.println("ID,Họ và tên,Vai trò,GPA,Email");
                List<Student> list = studentDAO.getAll();
                for (Student s : list) {
                    pw.printf("%s,%s,%s,%.2f,%s\n", 
                        s.getId(), 
                        s.getName().replace(",", " "), 
                        s.getMajor().replace(",", " "), 
                        s.getGpa(), 
                        s.getEmail()
                    );
                }
                JOptionPane.showMessageDialog(this, "Đã xuất dữ liệu ra file " + file.getName(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi ghi file CSV: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
