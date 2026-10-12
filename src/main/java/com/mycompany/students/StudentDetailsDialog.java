package com.mycompany.students;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

public class StudentDetailsDialog extends JDialog {
    public StudentDetailsDialog(
            JFrame parent,
            Student student,
            String teacherName) {
        super(parent, "Student Details", true);
        setSize(640, 620);
        setLocationRelativeTo(parent);
        UITheme.styleDialog(this);
        setLayout(new BorderLayout(12, 12));

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(BorderFactory.createEmptyBorder(14, 16, 8, 16));
        JPanel details = new JPanel(new GridLayout(6, 2, 12, 12));
        UITheme.styleSection(details, "STUDENT PROFILE");
        addDetail(details, "Student ID:", student.getStudentID());
        addDetail(details, "Name:", student.getName());
        addDetail(details, "Course:", student.getCourse());
        addDetail(details, "Year Level:", student.getYearLevel());
        addDetail(details, "Section:", student.getSection());
        addDetail(details, "Account Status:", student.getStatus());
        content.add(details, BorderLayout.NORTH);

        JPanel requirements = new JPanel(new BorderLayout(8, 8));
        UITheme.styleSection(requirements, "ENROLLMENT REQUIREMENTS");
        JProgressBar progress = new JProgressBar(
                0, student.getTotalRequirements());
        progress.setValue(student.getCompletedRequirements());
        progress.setStringPainted(true);
        progress.setString(
                student.getCompletedRequirements() + " of "
                        + student.getTotalRequirements() + " completed"
        );
        progress.setForeground(UITheme.TEAL);
        JPanel checklist = new JPanel(new GridLayout(2, 2, 8, 6));
        checklist.add(requirementLabel(
                "Information submitted", student.isInformationSubmitted()));
        checklist.add(requirementLabel(
                "Enrollment completed", student.isEnrollmentCompleted()));
        checklist.add(requirementLabel(
                "Activity 1 completed", student.isActivity1Completed()));
        checklist.add(requirementLabel(
                "Activity 2 completed", student.isActivity2Completed()));
        requirements.add(progress, BorderLayout.NORTH);
        requirements.add(checklist, BorderLayout.CENTER);
        content.add(requirements, BorderLayout.CENTER);

        JPanel remarksPanel = new JPanel(new BorderLayout(8, 8));
        UITheme.styleSection(remarksPanel, "RECENT TEACHER NOTES");
        List<Remark> remarks = student.getRemarks();
        Remark[] recentRemarks = remarks.stream().limit(5).toArray(Remark[]::new);
        JList<Remark> remarkList = new JList<>(recentRemarks);
        remarkList.setFont(UITheme.BODY_FONT);
        remarkList.setVisibleRowCount(4);
        remarksPanel.add(new JScrollPane(remarkList), BorderLayout.CENTER);
        if (remarks.isEmpty()) {
            remarksPanel.add(new JLabel("No teacher notes recorded yet."),
                    BorderLayout.SOUTH);
        }
        content.add(remarksPanel, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

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

    private JLabel requirementLabel(String label, boolean complete) {
        JLabel item = new JLabel(
                (complete ? "[x] " : "[ ] ") + label);
        item.setFont(UITheme.BODY_FONT);
        item.setForeground(complete ? UITheme.TEAL_DARK : UITheme.MUTED);
        return item;
    }
}
