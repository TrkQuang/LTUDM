package com.ltudm.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Cung cấp chủ đề giao diện phẳng (Flat Design) hiện đại, giống như giao diện Web.
 */
public class UITheme {
    public static final Color PRIMARY = new Color(37, 99, 235);       // Blue 600
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);  // Blue 700
    public static final Color SUCCESS = new Color(34, 197, 94);       // Green 500
    public static final Color SUCCESS_BG = new Color(220, 252, 231);  // Green 100
    public static final Color DANGER = new Color(239, 68, 68);        // Red 500
    public static final Color WARNING = new Color(245, 158, 11);      // Amber 500
    public static final Color PURPLE = new Color(139, 92, 246);       // Violet 500
    
    public static final Color BG_MAIN = new Color(248, 250, 252);     // Nền chính (Slate 50)
    public static final Color BG_SIDEBAR = new Color(15, 23, 42);     // Nền sidebar (Slate 900)
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);  // Nền sidebar hover (Slate 800)
    public static final Color SIDEBAR_ACTIVE = new Color(30, 58, 138); // Nền sidebar active (Blue 900)
    public static final Color SIDEBAR_TEXT = new Color(148, 163, 184); // Slate 400
    public static final Color SIDEBAR_TEXT_ACTIVE = Color.WHITE;

    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);      // Slate 900
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // Slate 200
    public static final Color CONSOLE_BG = new Color(15, 23, 42);      // Slate 900
    public static final Color CONSOLE_FG = new Color(241, 245, 249);   // Slate 100

    public static final Font FONT_TITLE = new Font("Inter", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Inter", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Inter", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Inter", Font.BOLD, 13);
    public static final Font FONT_MONO = new Font("JetBrains Mono", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Inter", Font.PLAIN, 11);

    public static void setupFlatLaf() {
        try {
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            
            // Try FlatLaf if available
            Class<?> clazz = Class.forName("com.formdev.flatlaf.FlatIntelliJLaf");
            UIManager.setLookAndFeel((LookAndFeel) clazz.getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
        UIManager.put("defaultFont", FONT_REGULAR);
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(SUCCESS);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
    
    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_DARK);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(7, 15, 7, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));
        return card;
    }

    public static Border createFieldBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        );
    }
    
    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setForeground(fg);
        badge.setFont(FONT_SMALL);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        badge.setOpaque(false);
        return badge;
    }
}
