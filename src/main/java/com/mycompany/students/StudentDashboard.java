package com.mycompany.students;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentDashboard extends JFrame {

    private Student student;

    private JLabel lblName;
    private JLabel lblID;
    private JLabel lblCourse;
    private JLabel lblYear;
    private JLabel lblSection;
    private JLabel lblStatus;

    private JTable attendanceTable;
    private JTable activitiesTable;
    private JTable scheduleTable;

    public StudentDashboard(Student student) {

        this.student = student;

        setTitle("Student Dashboard");

        setSize(820, 700);
        setMinimumSize(new Dimension(760, 600));

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        UITheme.styleWindow(this);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        buildUI();

        loadStudentData();
    }

    private void buildUI() {

        setLayout(new BorderLayout(10, 10));

        JPanel hero = UITheme.createHeroPanel(new BorderLayout());
        hero.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        JLabel title = new JLabel("MY STUDENT DASHBOARD");
        title.setForeground(Color.WHITE);
        title.setFont(UITheme.TITLE_FONT);
        JLabel subtitle = new JLabel("Your profile, attendance, activities, and class schedule");
        subtitle.setForeground(new Color(220, 241, 239));
        subtitle.setFont(UITheme.BODY_FONT);
        JPanel heroText = new JPanel(new GridLayout(2, 1, 0, 4));
        heroText.setOpaque(false);
        heroText.add(title);
        heroText.add(subtitle);
        hero.add(heroText, BorderLayout.CENTER);
        add(hero, BorderLayout.NORTH);

        JPanel main =
                new JPanel(
                        new BorderLayout(
                                10, 10
                        )
                );

        main.setBorder(
                BorderFactory.createEmptyBorder(
                        18, 26, 18, 26
                )
        );
        main.setBackground(UITheme.BACKGROUND);

        // ==========================================
        // PROFILE
        // ==========================================

        JPanel profile =
                new JPanel(
                        new GridLayout(
                                6, 2, 10, 10
                        )
                );

        UITheme.styleSection(profile, "STUDENT PROFILE");

        profile.add(
                new JLabel("Name:")
        );

        lblName =
                new JLabel();

        profile.add(lblName);

        profile.add(
                new JLabel("Student ID:")
        );

        lblID =
                new JLabel();

        profile.add(lblID);

        profile.add(
                new JLabel("Course:")
        );

        lblCourse =
                new JLabel();

        profile.add(lblCourse);

        profile.add(
                new JLabel("Year Level:")
        );

        lblYear =
                new JLabel();

        profile.add(lblYear);

        profile.add(new JLabel("Section:"));

        lblSection = new JLabel();
        profile.add(lblSection);

        profile.add(
                new JLabel("Account Status:")
        );

        lblStatus =
                new JLabel();

        profile.add(lblStatus);

        main.add(
                profile,
                BorderLayout.NORTH
        );

        JTabbedPane records = new JTabbedPane();
        attendanceTable = createReadOnlyTable(
                new String[]{"Date", "Attendance"});
        activitiesTable = createReadOnlyTable(
                new String[]{"Activity", "Date", "Status"});
        scheduleTable = createReadOnlyTable(
                new String[]{"Day", "Start", "End", "Subject", "Room"});
        records.addTab("Attendance", new JScrollPane(attendanceTable));
        records.addTab("Activities", new JScrollPane(activitiesTable));
        records.addTab("Class Schedule", new JScrollPane(scheduleTable));
        main.add(records, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        // ==========================================
        // BUTTONS
        // ==========================================

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT, 10, 12
                        )
                );
        buttons.setBackground(UITheme.SURFACE);
        buttons.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, UITheme.BORDER
        ));

        JButton refreshButton =
                new JButton("Refresh");

        JButton changePasswordButton =
                new JButton("Change Password");

        JButton logoutButton =
                new JButton("Logout");

        UITheme.styleButton(refreshButton, false);
        UITheme.styleButton(changePasswordButton, true);
        UITheme.styleButton(logoutButton, false);

        buttons.add(refreshButton);
        buttons.add(changePasswordButton);
        buttons.add(logoutButton);

        refreshButton.addActionListener(e ->
                refresh()
        );

        changePasswordButton
                .addActionListener(e ->
                        changePassword()
                );

        logoutButton.addActionListener(e -> {

            dispose();

            new LoginSelection()
                    .setVisible(true);
        });

        add(
                buttons,
                BorderLayout.SOUTH
        );
    }

    private void loadStudentData() {

        Student updated =
                StudentManager.findStudent(
                        student.getStudentID()
                );

        if (updated != null) {

            student = updated;
        }

        lblName.setText(
                student.getName()
        );

        lblID.setText(
                student.getStudentID()
        );

        lblCourse.setText(
                student.getCourse()
        );

        lblYear.setText(
                student.getYearLevel()
        );

        lblSection.setText(
                student.getSection()
        );

        lblStatus.setText(
                student.getStatus()
        );

        loadRecords();
    }

    private JTable createReadOnlyTable(String[] columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        UITheme.styleTable(table);
        return table;
    }

    private void loadRecords() {
        DefaultTableModel attendanceModel =
                (DefaultTableModel) attendanceTable.getModel();
        attendanceModel.setRowCount(0);
        for (StudentManager.AttendanceRecord record
                : StudentManager.getAttendance(student.getStudentID())) {
            attendanceModel.addRow(new Object[]{
                record.getDate(),
                record.getStatus()
            });
        }

        DefaultTableModel activitiesModel =
                (DefaultTableModel) activitiesTable.getModel();
        activitiesModel.setRowCount(0);
        for (StudentManager.StudentActivity activity
                : StudentManager.getStudentActivities(student.getStudentID())) {
            activitiesModel.addRow(new Object[]{
                activity.getName(),
                activity.getDate(),
                activity.getStatus()
            });
        }

        DefaultTableModel scheduleModel =
                (DefaultTableModel) scheduleTable.getModel();
        scheduleModel.setRowCount(0);
        for (StudentManager.ClassScheduleEntry entry
                : StudentManager.getClassSchedule(
                        student.getCourse(),
                        student.getYearLevel(),
                        student.getSection())) {
            scheduleModel.addRow(new Object[]{
                entry.getDay(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getSubject(),
                entry.getRoom()
            });
        }
    }

    private void refresh() {

        loadStudentData();

        JOptionPane.showMessageDialog(
                this,
                "Student dashboard refreshed."
        );
    }

    private void changePassword() {

        JPasswordField oldPassword =
                new JPasswordField();

        JPasswordField newPassword =
                new JPasswordField();

        JPasswordField confirmPassword =
                new JPasswordField();

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                4, 2, 10, 10
                        )
                );

        panel.add(
                new JLabel("Current Password:")
        );

        panel.add(oldPassword);

        panel.add(
                new JLabel("New Password:")
        );

        panel.add(newPassword);

        panel.add(
                new JLabel("Confirm Password:")
        );

        panel.add(confirmPassword);

        panel.add(
                new JLabel("Password must include:")
        );
        panel.add(
                new JLabel("uppercase, lowercase, number, symbol")
        );

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Change Password",
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String oldPass =
                new String(
                        oldPassword.getPassword()
                );

        String newPass =
                new String(
                        newPassword.getPassword()
                );

        String confirm =
                new String(
                        confirmPassword.getPassword()
                );

        String passwordError =
                PasswordPolicy.validationError(newPass);

        if (passwordError != null) {

            JOptionPane.showMessageDialog(
                    this,
                    passwordError,
                    "Invalid Password",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (!newPass.equals(confirm)) {

            JOptionPane.showMessageDialog(
                    this,
                    "New passwords do not match."
            );

            return;
        }

        boolean changed =
                StudentManager.changeStudentPassword(
                        student.getStudentID(),
                        oldPass,
                        newPass
                );

        if (changed) {

            JOptionPane.showMessageDialog(
                    this,
                    "Password changed successfully."
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Current password is incorrect."
            );
        }
    }
}
