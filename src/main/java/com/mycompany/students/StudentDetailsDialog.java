package com.mycompany.students;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StudentDetailsDialog extends JDialog {
    public StudentDetailsDialog(
            JFrame parent,
            Student student,
            String teacherName) {
        super(parent, "Student Details", true);
        setSize(560, 420);
        setLocationRelativeTo(parent);
        UITheme.styleDialog(this);
        setLayout(new BorderLayout(12, 12));

        JPanel details = new JPanel(new GridLayout(6, 2, 12, 12));
        UITheme.styleSection(details, "STUDENT PROFILE");
        addDetail(details, "Student ID:", student.getStudentID());
        addDetail(details, "Name:", student.getName());
        addDetail(details, "Course:", student.getCourse());
        addDetail(details, "Year Level:", student.getYearLevel());
        addDetail(details, "Section:", student.getSection());
        addDetail(details, "Account Status:", student.getStatus());
        add(details, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        UITheme.styleButton(close, false);
        close.addActionListener(event -> dispose());
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));
        footer.add(close, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    private void addDetail(JPanel panel, String label, String value) {
        panel.add(new JLabel(label));
        panel.add(new JLabel(value));
    }
}
