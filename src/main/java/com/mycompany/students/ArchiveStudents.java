package com.mycompany.students;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ArchiveStudents extends JDialog {

    private String teacherName;

    private JTable table;

    private DefaultTableModel model;

    public ArchiveStudents(
            JFrame parent,
            String teacherName) {

        super(
                parent,
                "Archived Students",
                true
        );

        this.teacherName = teacherName;

        setSize(1020, 640);

        setLocationRelativeTo(parent);
        UITheme.styleDialog(this);

        buildUI();

        loadArchive();
    }

    private void buildUI() {

        setLayout(new BorderLayout());

        JPanel hero = UITheme.createHeroPanel(new BorderLayout());
        hero.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("ARCHIVED STUDENTS");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Review archived accounts and restore students when needed"
        );
        subtitle.setFont(UITheme.BODY_FONT);
        subtitle.setForeground(new Color(220, 241, 239));
        JPanel heroText = new JPanel(new GridLayout(2, 1, 0, 4));
        heroText.setOpaque(false);
        heroText.add(title);
        heroText.add(subtitle);
        hero.add(heroText, BorderLayout.CENTER);
        add(hero, BorderLayout.NORTH);

        model =
                new DefaultTableModel(
                        new Object[]{
                                "Student ID",
                                "Name",
                                "Course",
                                "Year Level",
                                "Section",
                                "Status"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        table =
                new JTable(model);

        UITheme.styleTable(table);
        table.setAutoCreateRowSorter(true);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.setBackground(UITheme.BACKGROUND);
        tableArea.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        tableArea.add(scroll, BorderLayout.CENTER);
        add(tableArea, BorderLayout.CENTER);

        JPanel buttons =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT, 10, 10
                        )
                );
        buttons.setBackground(UITheme.SURFACE);
        buttons.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        JButton recover =
                new JButton(
                        "Recover Student"
                );

        JButton delete =
                new JButton(
                        "Permanent Delete"
                );

        JButton refresh =
                new JButton(
                        "Refresh"
                );

        JButton close =
                new JButton(
                        "Close"
                );

        UITheme.styleButton(recover, true);
        UITheme.styleButton(delete, false);
        UITheme.styleButton(refresh, false);
        UITheme.styleButton(close, false);

        buttons.add(recover);
        buttons.add(delete);
        buttons.add(refresh);
        buttons.add(close);

        add(
                buttons,
                BorderLayout.SOUTH
        );

        recover.addActionListener(e ->
                recoverStudent()
        );

        delete.addActionListener(e ->
                permanentlyDelete()
        );

        refresh.addActionListener(e ->
                loadArchive()
        );

        close.addActionListener(e ->
                dispose()
        );
    }

    private void loadArchive() {

        List<Student> students =
                StudentManager
                        .getArchivedStudents();

        model.setRowCount(0);

        for (Student student : students) {

            model.addRow(
                    new Object[]{
                            student.getStudentID(),
                            student.getName(),
                            student.getCourse(),
                            student.getYearLevel(),
                            student.getSection(),
                            student.getStatus()
                    }
            );
        }
    }

    private Student getSelectedStudent() {

        int viewRow =
                table.getSelectedRow();

        if (viewRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select an archived student."
            );

            return null;
        }

        int row = table.convertRowIndexToModel(viewRow);
        String id =
                model.getValueAt(
                        row,
                        0
                ).toString();

        return StudentManager.findStudent(id);
    }

    private void recoverStudent() {

        Student student =
                getSelectedStudent();

        if (student == null) {
            return;
        }

        String remark =
                JOptionPane.showInputDialog(
                        this,
                        "Enter recovery remark:\n"
                        + "Example: Student returned to class "
                        + "and completed the required follow-up."
                );

        if (
                remark == null
                        || remark.trim().isEmpty()
        ) {
            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Recover "
                                + student.getName()
                                + "?",
                        "Confirm Recovery",
                        JOptionPane.YES_NO_OPTION
                );

        if (
                confirm
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        if (
                StudentManager.recoverStudent(
                        student,
                        remark.trim(),
                        teacherName
                )
        ) {

            loadArchive();

            JOptionPane.showMessageDialog(
                    this,
                    "Student recovered successfully."
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The student could not be recovered. Refresh the archive and try again.",
                    "Recovery Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void permanentlyDelete() {

        Student student =
                getSelectedStudent();

        if (student == null) {
            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "WARNING!\n\n"
                        + "This will permanently delete:\n"
                        + "• Student account\n"
                        + "• Requirements\n"
                        + "• Remarks\n\n"
                        + "This cannot be undone.\n\n"
                        + "Continue?",
                        "Permanent Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

        if (
                confirm
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        if (
                StudentManager.permanentlyDelete(
                        student
                )
        ) {

            loadArchive();

            JOptionPane.showMessageDialog(
                    this,
                    "Student permanently deleted."
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The student could not be deleted. Refresh the archive and try again.",
                    "Delete Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
