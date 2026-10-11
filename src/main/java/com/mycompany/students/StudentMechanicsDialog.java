package com.mycompany.students;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
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

public class StudentMechanicsDialog extends JDialog {
    private final Student student;
    private final String teacherName;
    private final DefaultTableModel attendanceModel;
    private final DefaultTableModel activityModel;
    private final JTable attendanceTable;
    private final JTable activityTable;
    private final JTextField attendanceDate;
    private final JComboBox<String> attendanceStatus;
    private final JTextField activityName;
    private final JTextField activityDate;
    private final JComboBox<String> activityStatus;

    public StudentMechanicsDialog(
            JFrame parent,
            Student student,
            String teacherName) {
        super(parent, "Attendance and Activities", true);
        this.student = student;
        this.teacherName = teacherName;

        setSize(820, 700);
        setLocationRelativeTo(parent);
        UITheme.styleDialog(this);

        attendanceModel = new DefaultTableModel(
                new Object[]{"Date", "Attendance"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        activityModel = new DefaultTableModel(
                new Object[]{"Activity", "Date", "Status", "Record ID"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(attendanceModel);
        activityTable = new JTable(activityModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UITheme.styleTable(attendanceTable);
        UITheme.styleTable(activityTable);

        attendanceDate = new JTextField(LocalDate.now().toString(), 10);
        attendanceStatus = new JComboBox<>(
                new String[]{"Present", "Absent", "Late", "Excused"});
        activityName = new JTextField(20);
        activityDate = new JTextField(LocalDate.now().toString(), 10);
        activityStatus = new JComboBox<>(
                new String[]{"Assigned", "Completed"});

        buildUI();
        loadRecords();
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel content = new JPanel(new GridLayout(2, 1, 10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));

        JPanel attendancePanel = new JPanel(new BorderLayout(8, 8));
        UITheme.styleSection(attendancePanel, "ATTENDANCE - " + student.getName());
        JPanel attendanceForm = new JPanel(new BorderLayout(8, 0));
        JPanel attendanceFields = new JPanel(new GridLayout(1, 4, 8, 0));
        attendanceFields.add(new JLabel("Date (YYYY-MM-DD):"));
        attendanceFields.add(attendanceDate);
        attendanceFields.add(new JLabel("Status:"));
        attendanceFields.add(attendanceStatus);
        JButton saveAttendance = new JButton("Save Attendance");
        JButton deleteAttendance = new JButton("Delete Selected");
        UITheme.styleButton(saveAttendance, true);
        UITheme.styleButton(deleteAttendance, false);
        JPanel attendanceActions = new JPanel(new GridLayout(1, 2, 8, 0));
        attendanceActions.add(saveAttendance);
        attendanceActions.add(deleteAttendance);
        attendanceForm.add(attendanceFields, BorderLayout.CENTER);
        attendanceForm.add(attendanceActions, BorderLayout.EAST);
        attendancePanel.add(attendanceForm, BorderLayout.NORTH);
        attendancePanel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);
        content.add(attendancePanel);

        JPanel activityPanel = new JPanel(new BorderLayout(8, 8));
        UITheme.styleSection(activityPanel, "ACTIVITIES - " + student.getName());
        JPanel activityForm = new JPanel(new BorderLayout(8, 0));
        JPanel activityFields = new JPanel(new GridLayout(2, 3, 8, 6));
        activityFields.add(new JLabel("Activity:"));
        activityFields.add(new JLabel("Date (YYYY-MM-DD):"));
        activityFields.add(new JLabel("Status:"));
        activityFields.add(activityName);
        activityFields.add(activityDate);
        activityFields.add(activityStatus);
        JButton saveActivity = new JButton("Save Activity");
        JButton deleteActivity = new JButton("Delete Selected");
        UITheme.styleButton(saveActivity, true);
        UITheme.styleButton(deleteActivity, false);
        JPanel activityActions = new JPanel(new GridLayout(1, 2, 8, 0));
        activityActions.add(saveActivity);
        activityActions.add(deleteActivity);
        activityForm.add(activityFields, BorderLayout.CENTER);
        activityForm.add(activityActions, BorderLayout.EAST);
        activityPanel.add(activityForm, BorderLayout.NORTH);
        activityPanel.add(new JScrollPane(activityTable), BorderLayout.CENTER);
        activityTable.removeColumn(activityTable.getColumnModel().getColumn(3));
        content.add(activityPanel);
        add(content, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        UITheme.styleButton(close, false);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        footer.add(close, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        attendanceTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()
                    && attendanceTable.getSelectedRow() >= 0) {
                int row = attendanceTable.getSelectedRow();
                attendanceDate.setText(String.valueOf(
                        attendanceModel.getValueAt(row, 0)));
                attendanceStatus.setSelectedItem(
                        attendanceModel.getValueAt(row, 1));
            }
        });
        activityTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()
                    && activityTable.getSelectedRow() >= 0) {
                int row = activityTable.getSelectedRow();
                activityName.setText(String.valueOf(
                        activityModel.getValueAt(row, 0)));
                activityDate.setText(String.valueOf(
                        activityModel.getValueAt(row, 1)));
                activityStatus.setSelectedItem(
                        activityModel.getValueAt(row, 2));
            }
        });
        saveAttendance.addActionListener(event -> saveAttendance());
        deleteAttendance.addActionListener(event -> deleteAttendance());
        saveActivity.addActionListener(event -> saveActivity());
        deleteActivity.addActionListener(event -> deleteActivity());
        close.addActionListener(event -> dispose());
    }

    private void loadRecords() {
        attendanceModel.setRowCount(0);
        for (StudentManager.AttendanceRecord record
                : StudentManager.getAttendance(student.getStudentID())) {
            attendanceModel.addRow(new Object[]{
                record.getDate(),
                toTitleCase(record.getStatus())
            });
        }

        activityModel.setRowCount(0);
        for (StudentManager.StudentActivity activity
                : StudentManager.getStudentActivities(student.getStudentID())) {
            activityModel.addRow(new Object[]{
                activity.getName(),
                activity.getDate(),
                activity.getStatus(),
                activity.getId()
            });
        }
    }

    private void saveAttendance() {
        try {
            LocalDate date = LocalDate.parse(attendanceDate.getText().trim());
            String status = String.valueOf(attendanceStatus.getSelectedItem())
                    .toUpperCase(Locale.ROOT);
            if (StudentManager.saveAttendance(
                    student.getStudentID(), date, status, teacherName)) {
                loadRecords();
            }
        } catch (DateTimeParseException e) {
            showDateError();
        }
    }

    private void deleteAttendance() {
        int row = attendanceTable.getSelectedRow();
        if (row < 0) {
            showSelectionError("attendance record");
            return;
        }
        LocalDate date = LocalDate.parse(String.valueOf(
                attendanceModel.getValueAt(row, 0)));
        if (StudentManager.deleteAttendance(student.getStudentID(), date)) {
            loadRecords();
        }
    }

    private void saveActivity() {
        String name = activityName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Enter an activity name.", "Activity Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate date = LocalDate.parse(activityDate.getText().trim());
            int row = activityTable.getSelectedRow();
            Long id = row < 0
                    ? null
                    : Long.valueOf(String.valueOf(
                            activityModel.getValueAt(row, 3)));
            String status = String.valueOf(activityStatus.getSelectedItem());
            if (StudentManager.saveStudentActivity(
                    student.getStudentID(), id, name, date, status, teacherName)) {
                activityName.setText("");
                activityDate.setText(LocalDate.now().toString());
                activityTable.clearSelection();
                loadRecords();
            }
        } catch (DateTimeParseException e) {
            showDateError();
        }
    }

    private void deleteActivity() {
        int row = activityTable.getSelectedRow();
        if (row < 0) {
            showSelectionError("activity");
            return;
        }
        long id = Long.parseLong(String.valueOf(
                activityModel.getValueAt(row, 3)));
        if (StudentManager.deleteStudentActivity(student.getStudentID(), id)) {
            activityName.setText("");
            activityTable.clearSelection();
            loadRecords();
        }
    }

    private void showDateError() {
        JOptionPane.showMessageDialog(
                this, "Enter a valid date in YYYY-MM-DD format.",
                "Invalid Date", JOptionPane.WARNING_MESSAGE);
    }

    private String toTitleCase(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.charAt(0)
                + value.substring(1).toLowerCase(java.util.Locale.ROOT);
    }

    private void showSelectionError(String item) {
        JOptionPane.showMessageDialog(
                this, "Select an " + item + " first.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
    }
}
