package com.mycompany.students;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class ClassScheduleDialog extends JDialog {
    private final StudentManager.SectionGroup section;
    private final DefaultTableModel model;
    private final JTable table;
    private final JComboBox<String> day;
    private final JTextField startTime;
    private final JTextField endTime;
    private final JTextField subject;
    private final JTextField room;

    public ClassScheduleDialog(
            JFrame parent,
            StudentManager.SectionGroup section) {
        super(parent, "Class Schedule", true);
        this.section = section;
        setSize(850, 560);
        setLocationRelativeTo(parent);
        UITheme.styleDialog(this);

        model = new DefaultTableModel(
                new Object[]{"Day", "Start", "End", "Subject", "Room", "Record ID"},
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(table);
        day = new JComboBox<>(new String[]{
            "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"
        });
        startTime = new JTextField("08:00");
        endTime = new JTextField("09:00");
        subject = new JTextField();
        room = new JTextField();

        buildUI();
        loadEntries();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 10, 16));

        JPanel editor = new JPanel(new BorderLayout(8, 8));
        UITheme.styleSection(
                editor,
                "SECTION SCHEDULE - " + section.getCourse() + " / "
                        + section.getYearLevel() + " / " + section.getSection()
        );
        JPanel fields = new JPanel(new GridLayout(2, 5, 8, 6));
        fields.add(new JLabel("Day:"));
        fields.add(new JLabel("Start (HH:mm):"));
        fields.add(new JLabel("End (HH:mm):"));
        fields.add(new JLabel("Subject:"));
        fields.add(new JLabel("Room:"));
        fields.add(day);
        fields.add(startTime);
        fields.add(endTime);
        fields.add(subject);
        fields.add(room);
        JButton save = new JButton("Save Schedule Entry");
        JButton delete = new JButton("Delete Selected");
        JButton clear = new JButton("Clear");
        UITheme.styleButton(save, true);
        UITheme.styleButton(delete, false);
        UITheme.styleButton(clear, false);
        JPanel actions = new JPanel(new GridLayout(1, 3, 8, 0));
        actions.add(save);
        actions.add(delete);
        actions.add(clear);
        editor.add(fields, BorderLayout.NORTH);
        editor.add(actions, BorderLayout.SOUTH);
        content.add(editor, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        table.getColumnModel().getColumn(5).setMinWidth(0);
        table.getColumnModel().getColumn(5).setMaxWidth(0);
        table.getColumnModel().getColumn(5).setPreferredWidth(0);
        add(content, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        UITheme.styleButton(close, false);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        footer.add(close, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.getSelectedRow();
                day.setSelectedItem(model.getValueAt(row, 0));
                startTime.setText(String.valueOf(model.getValueAt(row, 1)));
                endTime.setText(String.valueOf(model.getValueAt(row, 2)));
                subject.setText(String.valueOf(model.getValueAt(row, 3)));
                room.setText(String.valueOf(model.getValueAt(row, 4)));
            }
        });
        save.addActionListener(event -> saveEntry());
        delete.addActionListener(event -> deleteEntry());
        clear.addActionListener(event -> clearForm());
        close.addActionListener(event -> dispose());
    }

    private void loadEntries() {
        model.setRowCount(0);
        for (StudentManager.ClassScheduleEntry entry
                : StudentManager.getClassSchedule(
                        section.getCourse(),
                        section.getYearLevel(),
                        section.getSection())) {
            model.addRow(new Object[]{
                entry.getDay(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getSubject(),
                entry.getRoom(),
                entry.getId()
            });
        }
    }

    private void saveEntry() {
        String subjectValue = subject.getText().trim();
        String roomValue = room.getText().trim();
        if (subjectValue.isEmpty() || roomValue.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Enter both a subject and room.",
                    "Schedule Details Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalTime start = LocalTime.parse(startTime.getText().trim());
            LocalTime end = LocalTime.parse(endTime.getText().trim());
            if (!start.isBefore(end)) {
                JOptionPane.showMessageDialog(
                        this, "End time must be later than start time.",
                        "Invalid Time", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int row = table.getSelectedRow();
            Long id = row < 0
                    ? null
                    : Long.valueOf(String.valueOf(model.getValueAt(row, 5)));
            if (StudentManager.saveClassSchedule(
                    section.getCourse(),
                    section.getYearLevel(),
                    section.getSection(),
                    id,
                    String.valueOf(day.getSelectedItem()),
                    start,
                    end,
                    subjectValue,
                    roomValue)) {
                clearForm();
                loadEntries();
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    this, "Enter start and end times in HH:mm format.",
                    "Invalid Time", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteEntry() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(
                    this, "Select a schedule entry first.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        long id = Long.parseLong(String.valueOf(model.getValueAt(row, 5)));
        if (StudentManager.deleteClassSchedule(
                section.getCourse(),
                section.getYearLevel(),
                section.getSection(),
                id)) {
            clearForm();
            loadEntries();
        }
    }

    private void clearForm() {
        table.clearSelection();
        subject.setText("");
        room.setText("");
        startTime.setText("08:00");
        endTime.setText("09:00");
        day.setSelectedIndex(0);
    }
}
