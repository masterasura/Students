package com.mycompany.students;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

public final class UITheme {

    public static final Color NAVY = new Color(16, 42, 56);
    public static final Color TEAL = new Color(0, 122, 113);
    public static final Color TEAL_DARK = new Color(0, 91, 86);
    public static final Color LIGHT_TEAL = new Color(222, 243, 239);
    public static final Color BACKGROUND = new Color(240, 247, 248);
    public static final Color SURFACE = new Color(255, 255, 255);
    public static final Color TEXT = new Color(35, 54, 65);
    public static final Color MUTED = new Color(105, 125, 135);
    public static final Color BORDER = new Color(218, 230, 233);
    public static final Color PALE_BLUE = new Color(224, 239, 244);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 25);

    private UITheme() {
    }

    public static void install() {
        Font body = BODY_FONT;
        Font label = LABEL_FONT;
        UIManager.put("defaultFont", body);
        UIManager.put("Label.font", body);
        UIManager.put("Button.font", label);
        UIManager.put("TextField.font", body);
        UIManager.put("PasswordField.font", body);
        UIManager.put("TextArea.font", body);
        UIManager.put("ComboBox.font", body);
        UIManager.put("Table.font", body);
        UIManager.put("TableHeader.font", label);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Button.background", LIGHT_TEAL);
        UIManager.put("Button.foreground", NAVY);
        UIManager.put("Button.select", TEAL);
        UIManager.put("Button.focus", BORDER);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEAL);
        UIManager.put("TextField.selectionBackground", LIGHT_TEAL);
        UIManager.put("TextField.selectionForeground", NAVY);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("PasswordField.caretForeground", TEAL);
        UIManager.put("ComboBox.background", SURFACE);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", LIGHT_TEAL);
        UIManager.put("Table.selectionForeground", NAVY);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", NAVY);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("ScrollPane.background", SURFACE);
        UIManager.put("Viewport.background", SURFACE);
        UIManager.put("CheckBox.background", SURFACE);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("TitledBorder.titleColor", NAVY);
    }

    public static void styleWindow(JFrame frame) {
        frame.getContentPane().setBackground(BACKGROUND);
    }

    public static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(BACKGROUND);
    }

    public static void styleTitle(JLabel label) {
        label.setForeground(NAVY);
        label.setFont(TITLE_FONT);
    }

    public static void styleSubtitle(JLabel label) {
        label.setForeground(MUTED);
        label.setFont(BODY_FONT);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)
        );
    }

    public static void styleCard(JPanel panel) {
        panel.setBackground(SURFACE);
        panel.setBorder(cardBorder());
    }

    public static void styleSection(JPanel panel, String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER),
                title,
                TitledBorder.LEADING,
                TitledBorder.TOP,
                LABEL_FONT,
                NAVY
        );
        panel.setBorder(BorderFactory.createCompoundBorder(
                border,
                BorderFactory.createEmptyBorder(10, 12, 12, 12)
        ));
        panel.setBackground(SURFACE);
    }

    public static void styleField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(TEXT);
        field.setBackground(SURFACE);
        field.setCaretColor(TEAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(BODY_FONT);
        area.setForeground(TEXT);
        area.setBackground(SURFACE);
        area.setCaretColor(TEAL);
        area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    }

    public static JPanel createHeroPanel(LayoutManager layout) {
        return new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY
                );
                g2.setPaint(new GradientPaint(
                        0, 0, NAVY,
                        getWidth(), getHeight(), TEAL_DARK
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(getWidth() - 160, -100, 260, 260);
                g2.fillOval(getWidth() - 80, getHeight() - 120, 200, 200);
                g2.dispose();
            }
        };
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(BODY_FONT);
        table.setForeground(TEXT);
        table.setBackground(SURFACE);
        table.setGridColor(BORDER);
        table.setSelectionBackground(LIGHT_TEAL);
        table.setSelectionForeground(NAVY);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setFont(LABEL_FONT);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer((tableHeader, value, selected, focused, row, column) -> {
            JLabel label = new JLabel(value == null ? "" : value.toString());
            label.setOpaque(true);
            label.setBackground(NAVY);
            label.setForeground(Color.WHITE);
            label.setFont(LABEL_FONT);
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return label;
        });
    }

    public static void styleButton(JButton button, boolean primary) {
        button.setFont(LABEL_FONT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(false);
        button.setForeground(primary ? Color.WHITE : NAVY);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setBackground(primary ? TEAL : LIGHT_TEAL);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void update(Graphics graphics, JComponent component) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );
                JButton current = (JButton) component;
                g2.setColor(current.getModel().isRollover()
                        ? (primary ? TEAL_DARK : PALE_BLUE)
                        : (primary ? TEAL : LIGHT_TEAL));
                g2.fillRoundRect(
                        0, 0, component.getWidth(), component.getHeight(), 12, 12
                );
                g2.dispose();
                paint(graphics, component);
            }
        });
    }
}
