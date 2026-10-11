package com.mycompany.students;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TeacherDashboard extends JFrame {

    private String teacherName;

    private JTable table;

    private DefaultTableModel model;

    private JTextField txtSearch;

    private JComboBox<String> statusFilter;

    private CardLayout pageLayout;
    private JPanel pages;
    private JList<StudentManager.SectionGroup> sectionList;
    private JList<Student> studentMatches;
    private JPanel directoryResults;
    private JTextField txtSectionSearch;
    private JLabel selectedSectionTitle;
    private StudentManager.SectionGroup selectedSection;

    public TeacherDashboard(
            String teacherName) {

        this.teacherName = teacherName;

        setTitle(
                "Teacher Dashboard - "
                        + teacherName
        );

        setSize(1120, 700);
        setMinimumSize(new Dimension(900, 600));

        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        UITheme.styleWindow(this);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        buildUI();

        loadSectionGroups();
    }

    private void buildUI() {

        setLayout(
                new BorderLayout(
                        10, 10
                )
        );

        // ==========================================
        // HEADER
        // ==========================================

        JPanel header = UITheme.createHeroPanel(new BorderLayout());

        JLabel title =
                new JLabel(
                        "TEACHER DASHBOARD"
                );

        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel welcome =
                new JLabel(
                        "Teacher: "
                                + teacherName
                );
        welcome.setForeground(new Color(220, 241, 239));
        welcome.setFont(UITheme.BODY_FONT);

        header.add(
                title,
                BorderLayout.WEST
        );

        header.add(
                welcome,
                BorderLayout.EAST
        );

        header.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        add(
                header,
                BorderLayout.NORTH
        );

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
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        table =
                new JTable(model);

        UITheme.styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        int[] columnWidths = {190, 185, 155, 110, 140, 145};
        for (int column = 0; column < columnWidths.length; column++) {
            table.getColumnModel().getColumn(column)
                    .setPreferredWidth(columnWidths[column]);
        }
        table.getColumnModel().getColumn(0).setMinWidth(175);
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(UITheme.SURFACE);
        top.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER),
                        BorderFactory.createEmptyBorder(9, 12, 9, 12)
                )
        );

        txtSearch =
                new JTextField(25);
        UITheme.styleField(txtSearch);

        statusFilter =
                new JComboBox<>(
                        new String[]{
                                "All current students",
                                "Active",
                                "Inactive",
                                "Dropped Out"
                        }
                );
        statusFilter.setFont(UITheme.BODY_FONT);
        statusFilter.setPreferredSize(new Dimension(175, 38));

        JButton searchButton =
                new JButton("Search");

        JButton refreshButton =
                new JButton("Refresh");

        UITheme.styleButton(searchButton, true);
        UITheme.styleButton(refreshButton, false);

        JPanel studentSearchPanel = new JPanel(new BorderLayout(8, 0));
        studentSearchPanel.setOpaque(false);
        studentSearchPanel.add(new JLabel("Student:"), BorderLayout.WEST);
        studentSearchPanel.add(txtSearch, BorderLayout.CENTER);
        studentSearchPanel.add(searchButton, BorderLayout.EAST);

        JPanel studentFilterPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        studentFilterPanel.setOpaque(false);
        studentFilterPanel.add(new JLabel("Status:"));
        studentFilterPanel.add(statusFilter);
        studentFilterPanel.add(refreshButton);
        top.add(studentSearchPanel, BorderLayout.CENTER);
        top.add(studentFilterPanel, BorderLayout.EAST);

        JPanel studentsPage = new JPanel(new BorderLayout(10, 10));
        studentsPage.setBackground(UITheme.BACKGROUND);
        studentsPage.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);
        selectedSectionTitle = new JLabel("Select a course and section");
        selectedSectionTitle.setFont(
                UITheme.BODY_FONT.deriveFont(Font.BOLD, 18f)
        );
        JButton backButton = new JButton("Back to Sections");
        UITheme.styleButton(backButton, false);
        backButton.addActionListener(e -> showSectionPage());
        sectionHeader.add(selectedSectionTitle, BorderLayout.CENTER);
        sectionHeader.add(backButton, BorderLayout.EAST);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setOpaque(false);
        JPanel studentControls = new JPanel(new BorderLayout(0, 10));
        studentControls.setOpaque(false);
        studentControls.add(sectionHeader, BorderLayout.NORTH);
        studentControls.add(top, BorderLayout.CENTER);
        tablePanel.add(studentControls, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        tablePanel.add(scroll, BorderLayout.CENTER);
        studentsPage.add(tablePanel, BorderLayout.CENTER);

        JPanel sectionsPage = new JPanel(new BorderLayout(10, 10));
        sectionsPage.setBackground(UITheme.BACKGROUND);
        sectionsPage.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel sectionsHeading = new JLabel("COURSE / YEAR / SECTION");
        sectionsHeading.setFont(UITheme.BODY_FONT.deriveFont(Font.BOLD, 20f));
        JLabel sectionsHelp = new JLabel(
                "Type the start of a student's name to see matches, or choose a section."
        );
        JPanel sectionsLabels = new JPanel(new GridLayout(2, 1, 0, 4));
        sectionsLabels.setOpaque(false);
        sectionsLabels.add(sectionsHeading);
        sectionsLabels.add(sectionsHelp);
        txtSectionSearch = new JTextField();
        UITheme.styleField(txtSectionSearch);
        txtSectionSearch.setToolTipText("Type the beginning of a student's name");
        JButton directorySearchButton = new JButton("Search");
        UITheme.styleButton(directorySearchButton, true);
        JPanel directorySearch = new JPanel(new BorderLayout(10, 0));
        directorySearch.setOpaque(false);
        directorySearch.add(txtSectionSearch, BorderLayout.CENTER);
        directorySearch.add(directorySearchButton, BorderLayout.EAST);
        JPanel directoryHeader = new JPanel(new BorderLayout(0, 14));
        directoryHeader.setOpaque(false);
        directoryHeader.add(sectionsLabels, BorderLayout.NORTH);
        directoryHeader.add(directorySearch, BorderLayout.SOUTH);
        sectionsPage.add(directoryHeader, BorderLayout.NORTH);
        sectionList = new JList<>();
        sectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sectionList.setFont(UITheme.BODY_FONT);
        sectionList.setFixedCellHeight(78);
        sectionList.setBackground(UITheme.BACKGROUND);
        sectionList.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        sectionList.setCellRenderer(new SectionGroupRenderer());
        studentMatches = new JList<>();
        studentMatches.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentMatches.setFont(UITheme.BODY_FONT);
        studentMatches.setFixedCellHeight(38);
        studentMatches.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel item = new JLabel(
                    value.getName() + " - " + value.getStudentID());
            item.setOpaque(true);
            item.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            item.setBackground(isSelected ? UITheme.TEAL_DARK : UITheme.SURFACE);
            item.setForeground(isSelected ? Color.WHITE : UITheme.TEXT);
            return item;
        });
        JScrollPane sectionScroll = new JScrollPane(sectionList);
        sectionScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        JScrollPane studentMatchesScroll = new JScrollPane(studentMatches);
        studentMatchesScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        directoryResults = new JPanel(new CardLayout());
        directoryResults.add(sectionScroll, "sections");
        directoryResults.add(studentMatchesScroll, "students");
        sectionsPage.add(directoryResults, BorderLayout.CENTER);

        pageLayout = new CardLayout();
        pages = new JPanel(pageLayout);
        pages.add(sectionsPage, "sections");
        pages.add(studentsPage, "students");
        add(pages, BorderLayout.CENTER);

        sectionList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                StudentManager.SectionGroup group = sectionList.getSelectedValue();
                if (group != null) {
                    openSection(group);
                }
            }
        });

        directorySearchButton.addActionListener(e ->
                searchSectionDirectory()
        );
        txtSectionSearch.addActionListener(e ->
                searchSectionDirectory()
        );
        txtSectionSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateDirectorySearch();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateDirectorySearch();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateDirectorySearch();
            }
        });
        studentMatches.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                Student student = studentMatches.getSelectedValue();
                if (student != null && openSectionFor(student)) {
                    loadStudents();
                }
            }
        });

        searchButton.addActionListener(e ->
                search()
        );

        statusFilter.addActionListener(e ->
                search()
        );

        refreshButton.addActionListener(e ->
                loadStudents()
        );
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                loadStudents();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                loadStudents();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                loadStudents();
            }
        });

        // ==========================================
        // BOTTOM BUTTONS
        // ==========================================

        JPanel bottom = new JPanel(new GridLayout(1, 5, 8, 6));
        bottom.setBackground(UITheme.SURFACE);
        bottom.setBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0, UITheme.BORDER
                )
        );

        JButton detailsButton =
                new JButton("View Details");
        JButton attendanceActivitiesButton =
                new JButton("Attendance & Activities");
        JButton scheduleButton =
                new JButton("Manage Class Schedule");
        JButton refreshRecordsButton =
                new JButton("Refresh");

        JButton logoutButton =
                new JButton("Logout");

        UITheme.styleButton(detailsButton, false);
        UITheme.styleButton(attendanceActivitiesButton, true);
        UITheme.styleButton(scheduleButton, true);
        UITheme.styleButton(refreshRecordsButton, false);
        UITheme.styleButton(logoutButton, false);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));

        bottom.add(detailsButton);
        bottom.add(attendanceActivitiesButton);
        bottom.add(scheduleButton);
        bottom.add(refreshRecordsButton);
        bottom.add(logoutButton);

        add(
                bottom,
                BorderLayout.SOUTH
        );

        // ==========================================
        // BUTTON ACTIONS
        // ==========================================

        detailsButton.addActionListener(e ->
                viewDetails()
        );
        attendanceActivitiesButton.addActionListener(e -> {
            Student student = getSelectedStudent();
            if (student != null) {
                new StudentMechanicsDialog(this, student, teacherName)
                        .setVisible(true);
            }
        });
        scheduleButton.addActionListener(e -> {
            if (selectedSection == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Select a section first.",
                        "No Section Selected",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            new ClassScheduleDialog(this, selectedSection).setVisible(true);
        });
        refreshRecordsButton.addActionListener(e -> {
            loadStudents();
            updateDirectorySearch();
        });

        logoutButton.addActionListener(e -> {

            dispose();

            new LoginSelection()
                    .setVisible(true);
        });
    }

    // =====================================================
    // LOAD STUDENTS
    // =====================================================

    private void loadStudents() {
        if (selectedSection == null) {
            return;
        }
        fillStudentTable(
                StudentManager.getStudentsBySection(
                        selectedSection.getCourse(),
                        selectedSection.getYearLevel(),
                        selectedSection.getSection(),
                        getSelectedStatus(),
                        txtSearch.getText()
                )
        );
    }

    // =====================================================
    // SEARCH
    // =====================================================

    private void search() {
        loadStudents();
    }

    private void fillStudentTable(List<Student> students) {
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

    // =====================================================
    // GET SELECTED STUDENT
    // =====================================================

    private Student getSelectedStudent() {

        int viewRow =
                table.getSelectedRow();

        if (viewRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a student first."
            );

            return null;
        }

        int row = table.convertRowIndexToModel(viewRow);
        String studentID =
                model.getValueAt(
                        row,
                        0
                ).toString();

        Student student = StudentManager.findStudent(
                studentID
        );

        if (student != null
                && "ARCHIVED".equals(student.getStatus())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Archived students are read-only here. "
                            + "Manage them from Archived Students.",
                    "Archived Student",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return null;
        }

        return student;
    }

    private String getSelectedStatus() {
        int selected = statusFilter.getSelectedIndex();
        switch (selected) {
            case 1:
                return "ACTIVE";
            case 2:
                return "INACTIVE";
            case 3:
                return "DROPPED OUT";
            default:
                return "ALL";
        }
    }

    private void loadSectionGroups() {
        loadSectionGroups(txtSectionSearch.getText());
    }

    private void loadSectionGroups(String studentName) {
        List<StudentManager.SectionGroup> groups =
                StudentManager.getSectionGroups(studentName);
        sectionList.setListData(
                groups.toArray(new StudentManager.SectionGroup[0])
        );
    }

    private void searchSectionDirectory() {
        updateDirectorySearch();
    }

    private void updateDirectorySearch() {
        String keyword = txtSectionSearch.getText().trim();
        loadSectionGroups(keyword);
        if (keyword.isEmpty()) {
            studentMatches.setListData(new Student[0]);
            ((CardLayout) directoryResults.getLayout()).show(
                    directoryResults, "sections");
            return;
        }
        List<Student> matches = StudentManager.searchStudentsByName(keyword);
        matches.removeIf(student ->
                CourseCatalog.canonicalCode(student.getCourse()) == null
        );
        studentMatches.setListData(matches.toArray(new Student[0]));
        ((CardLayout) directoryResults.getLayout()).show(
                directoryResults, "students");
    }

    private void openSection(StudentManager.SectionGroup group) {
        selectedSection = group;
        selectedSectionTitle.setText(
                group.getCourse() + "  |  " + group.getYearLevel()
                        + "  |  " + group.getSection()
        );
        txtSearch.setText(txtSectionSearch.getText().trim());
        statusFilter.setSelectedIndex(0);
        loadStudents();
        pageLayout.show(pages, "students");
    }

    private void showSectionPage() {
        selectedSection = null;
        pageLayout.show(pages, "sections");
    }

    private boolean openSectionFor(Student student) {
        String studentCourse =
                CourseCatalog.canonicalCode(student.getCourse());
        if (studentCourse == null) {
            return false;
        }
        for (int index = 0; index < sectionList.getModel().getSize(); index++) {
            StudentManager.SectionGroup group =
                    sectionList.getModel().getElementAt(index);
            if (group.getCourse().equals(studentCourse)
                    && group.getYearLevel().equals(student.getYearLevel())
                    && group.getSection().equals(student.getSection())) {
                openSection(group);
                txtSearch.setText(student.getName());
                loadStudents();
                return true;
            }
        }
        showSectionPage();
        return false;
    }

    private void changeStudentStatus() {
        Student student = getSelectedStudent();
        if (student == null) {
            return;
        }

        Object[] choices = {"Active", "Inactive", "Dropped Out"};
        int selected = JOptionPane.showOptionDialog(
                this,
                "Choose the student's status:",
                "Change Student Status",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                choices,
                choices[0]
        );
        if (selected < 0) {
            return;
        }

        String[] statusCodes = {"ACTIVE", "INACTIVE", "DROPPED OUT"};
        if (StudentManager.updateStudentStatus(
                student.getStudentID(),
                statusCodes[selected],
                teacherName
        )) {
            loadSectionGroups();
            loadStudents();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The student's status could not be updated.",
                    "Status Update Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // ADD STUDENT
    // =====================================================

    private void addStudent() {
        JTextField firstName =
                new JTextField();

        JTextField lastName =
                new JTextField();

        JComboBox<String>         course =
                new JComboBox<>(
                        CourseCatalog.getCourseCodes().toArray(new String[0])
                );
        course.setRenderer(new CourseRenderer());

        JComboBox<String> year =
                new JComboBox<>(new String[]{
                        "1st Year", "2nd Year", "3rd Year", "4th Year"
                });
        year.setEditable(true);
        UITheme.styleField(firstName);
        UITheme.styleField(lastName);
        course.setFont(UITheme.BODY_FONT);
        year.setFont(UITheme.BODY_FONT);

        JPanel panel =
                new JPanel(
                        new GridLayout(0, 2, 10, 10)
                );
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        panel.add(
                new JLabel("First name:")
        );

        panel.add(firstName);

        panel.add(
                new JLabel("Last name:")
        );

        panel.add(lastName);

        panel.add(
                new JLabel("Course:")
        );

        panel.add(course);

        panel.add(
                new JLabel("Year Level:")
        );

        panel.add(year);

        panel.setBorder(
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Add Student",
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String studentFirstName =
                firstName.getText().trim();

        String studentLastName =
                lastName.getText().trim();

        String studentCourse =
                (String) course.getSelectedItem();

        String studentYear =
                (String) year.getSelectedItem();

        if (
                studentFirstName.isEmpty()
                        || studentLastName.isEmpty()
                        || studentCourse == null
                        || studentCourse.trim().isEmpty()
                        || studentYear == null
                        || studentYear.trim().isEmpty()
        ) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please complete all fields."
            );

            return;
        }

        String studentPassword =
                studentLastName + studentFirstName;

        Student student =
                new Student(
                        "",
                        studentFirstName + " " + studentLastName,
                        studentCourse,
                        studentYear,
                        studentPassword
                );

        if (
                StudentManager.addStudent(
                        student
                )
        ) {

            txtSectionSearch.setText("");
            loadSectionGroups();
            openSectionFor(student);

            JOptionPane.showMessageDialog(
                    this,
                    "Student added successfully.\n\n"
                            + "Student ID: " + student.getStudentID() + "\n"
                            + "Course / Year: " + student.getCourse() + " / "
                            + student.getYearLevel() + "\n"
                            + "Section: " + student.getSection() + "\n"
                            + "Initial password: " + student.getPassword()
            );
        }
    }

    // =====================================================
    // EDIT STUDENT
    // =====================================================

    private void editStudent() {

        Student student =
                getSelectedStudent();

        if (student == null) {
            return;
        }

        JTextField name =
                new JTextField(
                        student.getName()
                );

        JComboBox<String> course =
                new JComboBox<>(
                        CourseCatalog.getCourseCodes().toArray(new String[0])
                );
        course.setRenderer(new CourseRenderer());
        String existingCourse =
                CourseCatalog.canonicalCode(student.getCourse());
        if (existingCourse == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "This student has a course that is not in the approved course list. "
                            + "Choose a valid course before saving.",
                    "Course Needs Correction",
                    JOptionPane.WARNING_MESSAGE
            );
            course.setSelectedIndex(-1);
        } else {
            course.setSelectedItem(existingCourse);
        }

        JComboBox<String> year =
                new JComboBox<>(new String[]{
                        "1st Year", "2nd Year", "3rd Year", "4th Year"
                });
        year.setEditable(true);
        year.setSelectedItem(student.getYearLevel());
        UITheme.styleField(name);
        course.setFont(UITheme.BODY_FONT);
        year.setFont(UITheme.BODY_FONT);

        JPanel panel =
                new JPanel(
                        new GridLayout(
                                3, 2, 10, 10
                        )
                );
        panel.setBackground(UITheme.SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        panel.add(
                new JLabel("Name:")
        );

        panel.add(name);

        panel.add(
                new JLabel("Course:")
        );

        panel.add(course);

        panel.add(
                new JLabel("Year Level:")
        );

        panel.add(year);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        panel,
                        "Edit Student",
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        if (course.getSelectedItem() == null
                || course.getSelectedItem().toString().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a course from the approved list.",
                    "Course Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        student.setName(
                name.getText().trim()
        );

        student.setCourse(
                (String) course.getSelectedItem()
        );

        student.setYearLevel(
                (String) year.getSelectedItem()
        );

        if (
                StudentManager.updateStudent(
                        student
                )
        ) {

            StudentManager.addRemark(
                    student.getStudentID(),
                    "Student information was updated.",
                    teacherName
            );

            txtSectionSearch.setText("");
            loadSectionGroups();
            openSectionFor(student);

            JOptionPane.showMessageDialog(
                    this,
                    "Student updated successfully."
            );
        }
    }

    // =====================================================
    // VIEW DETAILS
    // =====================================================

    private void viewDetails() {

        Student student =
                getSelectedStudent();

        if (student == null) {
            return;
        }

        new StudentDetailsDialog(
                this,
                student,
                teacherName
        ).setVisible(true);

        loadStudents();
    }

    // =====================================================
    // ARCHIVE
    // =====================================================

    private void archiveStudent() {

        Student student =
                getSelectedStudent();

        if (student == null) {
            return;
        }

        String reason =
                JOptionPane.showInputDialog(
                        this,
                        "Enter reason for archiving:"
                );

        if (
                reason == null
                        || reason.trim().isEmpty()
        ) {
            return;
        }

        int confirm =
                JOptionPane.showConfirmDialog(
                        this,
                        "Archive "
                                + student.getName()
                                + "?",
                        "Confirm Archive",
                        JOptionPane.YES_NO_OPTION
                );

        if (
                confirm
                        != JOptionPane.YES_OPTION
        ) {
            return;
        }

        if (
                StudentManager.archiveStudent(
                        student,
                        reason.trim(),
                        teacherName
                )
        ) {

            loadSectionGroups();
            loadStudents();

            JOptionPane.showMessageDialog(
                    this,
                    "Student archived successfully."
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "The student could not be archived. Refresh the list and try again.",
                    "Archive Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // ARCHIVE WINDOW
    // =====================================================

    private void openArchive() {

        new ArchiveStudents(
                this,
                teacherName
        ).setVisible(true);

        loadStudents();
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    private void resetPassword() {

        Student student =
                getSelectedStudent();

        if (student == null) {
            return;
        }

        JPasswordField password =
                new JPasswordField();
        UITheme.styleField(password);

        int result =
                JOptionPane.showConfirmDialog(
                        this,
                        password,
                        "New Temporary Password",
                        JOptionPane.OK_CANCEL_OPTION
                );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String newPassword =
                new String(
                        password.getPassword()
                );

        String passwordError =
                PasswordPolicy.validationError(newPassword);
        if (passwordError != null) {
            JOptionPane.showMessageDialog(
                    this,
                    passwordError,
                    "Invalid Password",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        if (
                StudentManager.resetStudentPassword(
                        student.getStudentID(),
                        newPassword
                )
        ) {

            StudentManager.addRemark(
                    student.getStudentID(),
                    "Teacher reset the student password.",
                    teacherName
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Student password reset successfully."
            );
        }
    }

    private static final class CourseRenderer
            extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean selected,
                boolean focused) {

            super.getListCellRendererComponent(
                    list,
                    value,
                    index,
                    selected,
                    focused
            );
            if (value != null) {
                String code = value.toString();
                setText(code + "  -  " + CourseCatalog.getDisplayName(code));
            }
            return this;
        }
    }

    private static final class SectionGroupRenderer
            extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean selected,
                boolean focused) {

            if (!(value instanceof StudentManager.SectionGroup)) {
                return super.getListCellRendererComponent(
                        list, value, index, selected, focused
                );
            }

            StudentManager.SectionGroup group =
                    (StudentManager.SectionGroup) value;
            JPanel card = new JPanel(new BorderLayout(14, 0));
            card.setOpaque(true);
            card.setBackground(selected ? UITheme.LIGHT_TEAL : UITheme.SURFACE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(
                            selected ? UITheme.TEAL : UITheme.BORDER
                    ),
                    BorderFactory.createEmptyBorder(10, 16, 10, 16)
            ));

            JPanel courseColumn = new JPanel(new GridBagLayout());
            courseColumn.setOpaque(false);
            courseColumn.setPreferredSize(new Dimension(96, 48));
            JLabel course = new JLabel(group.getCourse());
            course.setFont(UITheme.LABEL_FONT.deriveFont(15f));
            course.setForeground(selected ? UITheme.TEAL_DARK : UITheme.NAVY);
            courseColumn.add(course);
            card.add(courseColumn, BorderLayout.WEST);

            JPanel details = new JPanel(new GridLayout(2, 1, 0, 3));
            details.setOpaque(false);
            JLabel programName = new JLabel(
                    CourseCatalog.getDisplayName(group.getCourse())
            );
            programName.setForeground(UITheme.MUTED);
            JLabel section = new JLabel(
                    group.getYearLevel() + "  |  " + group.getSection()
            );
            section.setFont(UITheme.LABEL_FONT);
            section.setForeground(UITheme.TEXT);
            details.add(programName);
            details.add(section);
            card.add(details, BorderLayout.CENTER);

            JLabel open = new JLabel("VIEW SECTION  >");
            open.setForeground(UITheme.TEAL_DARK);
            open.setFont(UITheme.LABEL_FONT);
            card.add(open, BorderLayout.EAST);
            return card;
        }
    }
}
