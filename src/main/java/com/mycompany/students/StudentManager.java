package com.mycompany.students;


import java.sql.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.JOptionPane;

public class StudentManager {

    private static final String STUDENT_CREATION_LOCK =
            "student_management_create_student";

    public static final class SectionGroup {
        private final String course;
        private final String yearLevel;
        private final String section;
        private final int studentCount;

        private SectionGroup(
                String course,
                String yearLevel,
                String section,
                int studentCount) {
            this.course = course;
            this.yearLevel = yearLevel;
            this.section = section;
            this.studentCount = studentCount;
        }

        public String getCourse() {
            return course;
        }

        public String getYearLevel() {
            return yearLevel;
        }

        public String getSection() {
            return section;
        }

        @Override
        public String toString() {
            return course + "  |  " + yearLevel + "  |  "
                    + section + "  (" + studentCount + " students)";
        }
    }

    public static final class AttendanceRecord {
        private final LocalDate date;
        private final String status;

        private AttendanceRecord(LocalDate date, String status) {
            this.date = date;
            this.status = status;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }
    }

    public static final class StudentActivity {
        private final long id;
        private final String name;
        private final LocalDate date;
        private final String status;

        private StudentActivity(
                long id,
                String name,
                LocalDate date,
                String status) {
            this.id = id;
            this.name = name;
            this.date = date;
            this.status = status;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }
    }

    public static final class ClassScheduleEntry {
        private final long id;
        private final String day;
        private final LocalTime startTime;
        private final LocalTime endTime;
        private final String subject;
        private final String room;

        private ClassScheduleEntry(
                long id,
                String day,
                LocalTime startTime,
                LocalTime endTime,
                String subject,
                String room) {
            this.id = id;
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
            this.subject = subject;
            this.room = room;
        }

        public long getId() {
            return id;
        }

        public String getDay() {
            return day;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        public String getSubject() {
            return subject;
        }

        public String getRoom() {
            return room;
        }
    }

    static {
        ensureSectionColumn();
        normalizeCatalogCourses();
        normalizeAndRebalanceSections();
        migrateStudentIDs();
        ensureAcademicTrackingTables();
    }

    // =====================================================
    // STUDENT LOGIN
    // =====================================================

    public static Student loginStudent(
            String studentID,
            String password) {

        String sql =
                "SELECT * FROM students "
                + "WHERE student_id = ? "
                + "AND password = ? "
                + "AND status = 'ACTIVE'";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, studentID);
            stmt.setString(2, password);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {
                return createStudent(rs);
            }

        } catch (SQLException e) {
            showError(e);
        }

        return null;
    }

    // =====================================================
    // TEACHER LOGIN
    // =====================================================

    public static String loginTeacher(
            String username,
            String password) {

        String sql =
                "SELECT name FROM teachers "
                + "WHERE username = ? "
                + "AND password = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }

        } catch (SQLException e) {
            showError(e);
        }

        return null;
    }

    // =====================================================
    // GET ACTIVE STUDENTS
    // =====================================================

    public static List<Student>
    getActiveStudents() {

        return getStudentsByStatus("ACTIVE");
    }

    public static List<Student>
    getAllStudents() {

        return getStudentsByStatus("ALL");
    }

    public static List<SectionGroup> getSectionGroups() {
        return getSectionGroups("");
    }

    public static List<SectionGroup> getSectionGroups(String studentName) {
        List<SectionGroup> groups = new ArrayList<>();
        String sql =
                "SELECT s.course, s.year_level, s.section, "
                + "COUNT(*) AS student_count "
                + "FROM students s WHERE s.section IS NOT NULL AND s.section <> '' "
                + "AND s.status <> 'ARCHIVED' "
                + "AND (? = '' OR EXISTS ("
                + "SELECT 1 FROM students matching "
                + "WHERE matching.course = s.course "
                + "AND matching.year_level = s.year_level "
                + "AND matching.section = s.section "
                + "AND matching.status <> 'ARCHIVED' "
                + "AND matching.name LIKE ?)) "
                + "GROUP BY s.course, s.year_level, s.section "
                + "ORDER BY s.course, s.year_level, s.section";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String keyword = studentName == null ? "" : studentName.trim();
            stmt.setString(1, keyword);
            stmt.setString(2, keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String course = CourseCatalog.canonicalCode(
                            rs.getString("course")
                    );
                    String yearLevel = canonicalYearLevel(
                            rs.getString("year_level")
                    );
                    if (course != null && yearLevel != null) {
                        groups.add(new SectionGroup(
                                course,
                                yearLevel,
                                rs.getString("section"),
                                rs.getInt("student_count")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
        return groups;
    }

    public static List<String> getAvailableCourses() {
        return CourseCatalog.getCourseCodes();
    }

    public static List<Student> getStudentsBySection(
            String course,
            String yearLevel,
            String section,
            String status,
            String keyword) {

        List<Student> students = new ArrayList<>();
        boolean allStatuses = "ALL".equals(status);
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        String sql =
                "SELECT * FROM students "
                + "WHERE course = ? AND year_level = ? AND section = ? "
                + (allStatuses
                        ? "AND status <> 'ARCHIVED' "
                        : "AND status = ? ")
                + (hasKeyword
                        ? "AND (student_id LIKE ? OR name LIKE ?) "
                        : "")
                + "ORDER BY name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int parameter = 1;
            stmt.setString(parameter++, course);
            stmt.setString(parameter++, yearLevel);
            stmt.setString(parameter++, section);
            if (!allStatuses) {
                stmt.setString(parameter++, status);
            }
            if (hasKeyword) {
                String keywordPrefix = keyword.trim() + "%";
                stmt.setString(parameter++, "%" + keyword.trim() + "%");
                stmt.setString(parameter, keywordPrefix);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(createStudent(rs));
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
        return students;
    }

    public static List<Student> searchStudentsByName(String name) {
        List<Student> students = new ArrayList<>();
        String sql =
                "SELECT * FROM students WHERE name LIKE ? "
                + "AND status <> 'ARCHIVED' ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(createStudent(rs));
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
        return students;
    }

    public static List<AttendanceRecord> getAttendance(String studentID) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT attendance_date, attendance_status "
                + "FROM student_attendance WHERE student_id = ? "
                + "ORDER BY attendance_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(new AttendanceRecord(
                            rs.getDate("attendance_date").toLocalDate(),
                            rs.getString("attendance_status")
                    ));
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
        return records;
    }

    public static boolean saveAttendance(
            String studentID,
            LocalDate date,
            String status,
            String updatedBy) {
        String sql = "INSERT INTO student_attendance "
                + "(student_id, attendance_date, attendance_status, updated_by) "
                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                + "attendance_status = VALUES(attendance_status), "
                + "updated_by = VALUES(updated_by), "
                + "updated_at = CURRENT_TIMESTAMP";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentID);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setString(3, status);
            stmt.setString(4, updatedBy);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    public static boolean deleteAttendance(String studentID, LocalDate date) {
        String sql = "DELETE FROM student_attendance "
                + "WHERE student_id = ? AND attendance_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentID);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    public static List<StudentActivity> getStudentActivities(String studentID) {
        List<StudentActivity> activities = new ArrayList<>();
        String sql = "SELECT activity_id, activity_name, activity_date, status "
                + "FROM student_activities WHERE student_id = ? "
                + "ORDER BY activity_date DESC, activity_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(new StudentActivity(
                            rs.getLong("activity_id"),
                            rs.getString("activity_name"),
                            rs.getDate("activity_date").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
        return activities;
    }

    public static boolean saveStudentActivity(
            String studentID,
            Long activityID,
            String name,
            LocalDate date,
            String status,
            String updatedBy) {
        String sql = activityID == null
                ? "INSERT INTO student_activities "
                        + "(student_id, activity_name, activity_date, status, updated_by) "
                        + "VALUES (?, ?, ?, ?, ?)"
                : "UPDATE student_activities SET activity_name = ?, "
                        + "activity_date = ?, status = ?, updated_by = ? "
                        + "WHERE activity_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (activityID == null) {
                stmt.setString(1, studentID);
                stmt.setString(2, name);
                stmt.setDate(3, java.sql.Date.valueOf(date));
                stmt.setString(4, status);
                stmt.setString(5, updatedBy);
            } else {
                stmt.setString(1, name);
                stmt.setDate(2, java.sql.Date.valueOf(date));
                stmt.setString(3, status);
                stmt.setString(4, updatedBy);
                stmt.setLong(5, activityID);
                stmt.setString(6, studentID);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    public static boolean deleteStudentActivity(String studentID, long activityID) {
        String sql = "DELETE FROM student_activities "
                + "WHERE activity_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, activityID);
            stmt.setString(2, studentID);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    public static List<ClassScheduleEntry> getClassSchedule(
            String course,
            String yearLevel,
            String section) {
        List<ClassScheduleEntry> entries = new ArrayList<>();
        String sql = "SELECT schedule_id, day_of_week, start_time, end_time, "
                + "subject, room FROM class_schedule "
                + "WHERE course = ? AND year_level = ? AND section = ? "
                + "ORDER BY FIELD(day_of_week, 'Monday', 'Tuesday', 'Wednesday', "
                + "'Thursday', 'Friday', 'Saturday', 'Sunday'), start_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course);
            stmt.setString(2, yearLevel);
            stmt.setString(3, section);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entries.add(new ClassScheduleEntry(
                            rs.getLong("schedule_id"),
                            rs.getString("day_of_week"),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime(),
                            rs.getString("subject"),
                            rs.getString("room")
                    ));
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
        return entries;
    }

    public static boolean saveClassSchedule(
            String course,
            String yearLevel,
            String section,
            Long scheduleID,
            String day,
            LocalTime startTime,
            LocalTime endTime,
            String subject,
            String room) {
        String sql = scheduleID == null
                ? "INSERT INTO class_schedule "
                        + "(course, year_level, section, day_of_week, start_time, "
                        + "end_time, subject, room) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                : "UPDATE class_schedule SET day_of_week = ?, start_time = ?, "
                        + "end_time = ?, subject = ?, room = ? "
                        + "WHERE schedule_id = ? AND course = ? "
                        + "AND year_level = ? AND section = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (scheduleID == null) {
                stmt.setString(1, course);
                stmt.setString(2, yearLevel);
                stmt.setString(3, section);
                stmt.setString(4, day);
                stmt.setTime(5, java.sql.Time.valueOf(startTime));
                stmt.setTime(6, java.sql.Time.valueOf(endTime));
                stmt.setString(7, subject);
                stmt.setString(8, room);
            } else {
                stmt.setString(1, day);
                stmt.setTime(2, java.sql.Time.valueOf(startTime));
                stmt.setTime(3, java.sql.Time.valueOf(endTime));
                stmt.setString(4, subject);
                stmt.setString(5, room);
                stmt.setLong(6, scheduleID);
                stmt.setString(7, course);
                stmt.setString(8, yearLevel);
                stmt.setString(9, section);
            }
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    public static boolean deleteClassSchedule(
            String course,
            String yearLevel,
            String section,
            long scheduleID) {
        String sql = "DELETE FROM class_schedule WHERE schedule_id = ? "
                + "AND course = ? AND year_level = ? AND section = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, scheduleID);
            stmt.setString(2, course);
            stmt.setString(3, yearLevel);
            stmt.setString(4, section);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    private static void ensureAcademicTrackingTables() {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS student_attendance ("
                    + "student_id VARCHAR(255) NOT NULL, "
                    + "attendance_date DATE NOT NULL, "
                    + "attendance_status VARCHAR(30) NOT NULL, "
                    + "updated_by VARCHAR(255) NOT NULL, "
                    + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP "
                    + "ON UPDATE CURRENT_TIMESTAMP, "
                    + "PRIMARY KEY (student_id, attendance_date))",
            "CREATE TABLE IF NOT EXISTS student_activities ("
                    + "activity_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "student_id VARCHAR(255) NOT NULL, "
                    + "activity_name VARCHAR(255) NOT NULL, "
                    + "activity_date DATE NOT NULL, "
                    + "status VARCHAR(30) NOT NULL, "
                    + "updated_by VARCHAR(255) NOT NULL)",
            "CREATE TABLE IF NOT EXISTS class_schedule ("
                    + "schedule_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                    + "course VARCHAR(100) NOT NULL, "
                    + "year_level VARCHAR(100) NOT NULL, "
                    + "section VARCHAR(100) NOT NULL, "
                    + "day_of_week VARCHAR(20) NOT NULL, "
                    + "start_time TIME NOT NULL, "
                    + "end_time TIME NOT NULL, "
                    + "subject VARCHAR(255) NOT NULL, "
                    + "room VARCHAR(100) NOT NULL)"
        };
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    public static boolean updateStudentStatus(
            String studentID,
            String status,
            String teacherName) {

        if (!"ACTIVE".equals(status)
                && !"INACTIVE".equals(status)
                && !"DROPPED OUT".equals(status)) {
            throw new IllegalArgumentException("Unsupported student status.");
        }

        String sql =
                "UPDATE students SET status = ? "
                + "WHERE student_id = ? AND status <> 'ARCHIVED'";
        try (Connection conn = DBConnection.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, status);
                    stmt.setString(2, studentID);
                    if (stmt.executeUpdate() != 1) {
                        try (PreparedStatement check = conn.prepareStatement(
                                "SELECT status FROM students WHERE student_id = ?"
                        )) {
                            check.setString(1, studentID);
                            try (ResultSet rs = check.executeQuery()) {
                                if (rs.next()
                                        && status.equals(rs.getString("status"))) {
                                    conn.rollback();
                                    return true;
                                }
                            }
                        }
                        conn.rollback();
                        return false;
                    }
                }
                addRemark(
                        conn,
                        studentID,
                        "Student status changed to " + status + ".",
                        teacherName
                );
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    // =====================================================
    // GET ARCHIVED STUDENTS
    // =====================================================

    public static List<Student>
    getArchivedStudents() {

        List<Student> students =
                new ArrayList<>();

        String sql =
                "SELECT s.* FROM students s "
                + "INNER JOIN archived_students a "
                + "ON a.student_id = s.student_id "
                + "WHERE s.status = 'ARCHIVED' "
                + "ORDER BY s.name";

        try (Connection conn = DBConnection.getConnection()) {

            ensureArchiveTable(conn);

            try (
                    PreparedStatement stmt =
                            conn.prepareStatement(sql);

                    ResultSet rs =
                            stmt.executeQuery()
            ) {
                while (rs.next()) {
                    students.add(createStudent(rs));
                }
            }

        } catch (SQLException e) {
            showError(e);
        }

        return students;
    }

    private static List<Student>
    getStudentsByStatus(String status) {

        List<Student> students =
                new ArrayList<>();

        boolean allStatuses = "ALL".equals(status);

        String sql =
                "SELECT * FROM students "
                + (allStatuses ? "" : "WHERE status = ? ")
                + "ORDER BY name";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            if (!allStatuses) {
                stmt.setString(1, status);
            }

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                students.add(
                        createStudent(rs)
                );
            }

        } catch (SQLException e) {
            showError(e);
        }

        return students;
    }

    // =====================================================
    // FIND STUDENT
    // =====================================================

    public static Student findStudent(
            String studentID) {

        String sql =
                "SELECT * FROM students "
                + "WHERE student_id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, studentID);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {
                return createStudent(rs);
            }

        } catch (SQLException e) {
            showError(e);
        }

        return null;
    }

    // =====================================================
    // CHECK STUDENT ID
    // =====================================================

    public static boolean studentIDExists(
            String studentID) {

        String sql =
                "SELECT COUNT(*) "
                + "FROM students "
                + "WHERE student_id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, studentID);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            showError(e);
        }

        return false;
    }

    // =====================================================
    // ADD STUDENT
    // =====================================================

    public static boolean addStudent(
            Student student) {

        String canonicalCourse =
                CourseCatalog.canonicalCode(student.getCourse());
        String canonicalYear =
                canonicalYearLevel(student.getYearLevel());
        if (canonicalCourse == null || canonicalYear == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Select a valid course and year level before adding the student.",
                    "Invalid Student Course or Year",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        student.setCourse(canonicalCourse);
        student.setYearLevel(canonicalYear);

        String sql =
                "INSERT INTO students "
                + "(student_id, name, course, "
                + "year_level, section, password, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";

        try (Connection conn = DBConnection.getConnection()) {
            boolean lockAcquired = false;
            try {
                acquireStudentCreationLock(conn);
                lockAcquired = true;
                conn.setAutoCommit(false);

                student.setSection(
                        findAvailableSection(
                                conn,
                                student.getCourse(),
                                student.getYearLevel(),
                                null
                        )
                );
                student.setStudentID(
                        generateStudentID(
                                conn,
                                student.getYearLevel()
                        )
                );

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, student.getStudentID());
                    stmt.setString(2, student.getName());
                    stmt.setString(3, student.getCourse());
                    stmt.setString(4, student.getYearLevel());
                    stmt.setString(5, student.getSection());
                    stmt.setString(6, student.getPassword());
                    stmt.executeUpdate();
                }

                createRequirements(conn, student.getStudentID());
                addRemark(
                        conn,
                        student.getStudentID(),
                        "Student account created.",
                        "Teacher"
                );
                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                if (lockAcquired) {
                    releaseStudentCreationLock(conn);
                }
            }

        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    // =====================================================
    // CREATE REQUIREMENTS
    // =====================================================

    private static void createRequirements(
            Connection conn,
            String studentID)
            throws SQLException {

        String sql =
                "INSERT INTO requirements "
                + "(student_id, information_submitted, "
                + "enrollment_completed, activity1_completed, "
                + "activity2_completed) "
                + "VALUES (?, FALSE, FALSE, FALSE, FALSE)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentID);
            stmt.executeUpdate();
        }
    }

    // =====================================================
    // UPDATE STUDENT
    // =====================================================

    public static boolean updateStudent(
            Student student) {

        String canonicalCourse =
                CourseCatalog.canonicalCode(student.getCourse());
        String canonicalYear =
                canonicalYearLevel(student.getYearLevel());
        if (canonicalCourse == null || canonicalYear == null) {
            JOptionPane.showMessageDialog(
                    null,
                    "Select a valid course and year level before saving.",
                    "Invalid Student Course or Year",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        student.setCourse(canonicalCourse);
        student.setYearLevel(canonicalYear);

        String sql =
                "UPDATE students SET "
                + "name = ?, "
                + "course = ?, "
                + "year_level = ?, "
                + "section = ? "
                + "WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            boolean lockAcquired = false;
            try {
                acquireStudentCreationLock(conn);
                lockAcquired = true;
                conn.setAutoCommit(false);

                student.setSection(
                        findAvailableSection(
                                conn,
                                student.getCourse(),
                                student.getYearLevel(),
                                student.getStudentID()
                        )
                );

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, student.getName());
                    stmt.setString(2, student.getCourse());
                    stmt.setString(3, student.getYearLevel());
                    stmt.setString(4, student.getSection());
                    stmt.setString(5, student.getStudentID());
                    stmt.executeUpdate();
                }
                conn.commit();
                updateRequirements(student);
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                if (lockAcquired) {
                    releaseStudentCreationLock(conn);
                }
            }

        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    // =====================================================
    // UPDATE REQUIREMENTS
    // =====================================================

    public static boolean updateRequirements(
            Student student) {

        String sql =
                "UPDATE requirements SET "
                + "information_submitted = ?, "
                + "enrollment_completed = ?, "
                + "activity1_completed = ?, "
                + "activity2_completed = ? "
                + "WHERE student_id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setBoolean(
                    1,
                    student.isInformationSubmitted()
            );

            stmt.setBoolean(
                    2,
                    student.isEnrollmentCompleted()
            );

            stmt.setBoolean(
                    3,
                    student.isActivity1Completed()
            );

            stmt.setBoolean(
                    4,
                    student.isActivity2Completed()
            );

            stmt.setString(
                    5,
                    student.getStudentID()
            );

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {

            showError(e);

            return false;
        }
    }

    // =====================================================
    // ARCHIVE STUDENT
    // =====================================================

    public static boolean archiveStudent(
            Student student,
            String reason,
            String teacherName) {

        String updateSql =
                "UPDATE students SET status = 'ARCHIVED' "
                + "WHERE student_id = ? AND status = 'ACTIVE'";

        String archiveSql =
                "INSERT INTO archived_students "
                + "(student_id, archive_reason, archived_by) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "archived_at = CURRENT_TIMESTAMP, "
                + "archive_reason = ?, archived_by = ?";

        try (Connection conn = DBConnection.getConnection()) {
            ensureArchiveTable(conn);
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                int updated;
                try (PreparedStatement stmt =
                             conn.prepareStatement(updateSql)) {
                    stmt.setString(1, student.getStudentID());
                    updated = stmt.executeUpdate();
                }

                if (updated != 1) {
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement stmt =
                             conn.prepareStatement(archiveSql)) {
                    stmt.setString(1, student.getStudentID());
                    stmt.setString(2, reason);
                    stmt.setString(3, teacherName);
                    stmt.setString(4, reason);
                    stmt.setString(5, teacherName);
                    stmt.executeUpdate();
                }

                addRemark(
                        conn,
                        student.getStudentID(),
                        "Student archived. Reason: " + reason,
                        teacherName
                );

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {

            showError(e);

            return false;
        }
    }

    // =====================================================
    // RECOVER STUDENT
    // =====================================================

    public static boolean recoverStudent(
            Student student,
            String recoveryRemark,
            String teacherName) {

        String findStudentSql =
                "SELECT course, year_level FROM students "
                + "WHERE student_id = ? AND status = 'ARCHIVED'";

        String updateSql =
                "UPDATE students SET status = 'ACTIVE', section = ? "
                + "WHERE student_id = ? AND status = 'ARCHIVED'";

        String deleteArchiveSql =
                "DELETE FROM archived_students WHERE student_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            ensureArchiveTable(conn);
            boolean originalAutoCommit = conn.getAutoCommit();
            boolean lockAcquired = false;
            try {
                acquireStudentCreationLock(conn);
                lockAcquired = true;
                conn.setAutoCommit(false);

                String course;
                String yearLevel;
                try (PreparedStatement stmt =
                             conn.prepareStatement(findStudentSql)) {
                    stmt.setString(1, student.getStudentID());
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        course = rs.getString("course");
                        yearLevel = rs.getString("year_level");
                    }
                }

                String section = findAvailableSection(
                        conn,
                        course,
                        yearLevel,
                        student.getStudentID()
                );
                int updated;
                try (PreparedStatement stmt =
                             conn.prepareStatement(updateSql)) {
                    stmt.setString(1, section);
                    stmt.setString(2, student.getStudentID());
                    updated = stmt.executeUpdate();
                }

                if (updated != 1) {
                    conn.rollback();
                    return false;
                }

                student.setSection(section);
                student.setStatus("ACTIVE");
                try (PreparedStatement stmt =
                             conn.prepareStatement(deleteArchiveSql)) {
                    stmt.setString(1, student.getStudentID());
                    stmt.executeUpdate();
                }

                addRemark(
                        conn,
                        student.getStudentID(),
                        "Student recovered. " + recoveryRemark,
                        teacherName
                );

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
                if (lockAcquired) {
                    releaseStudentCreationLock(conn);
                }
            }

        } catch (SQLException e) {

            showError(e);

            return false;
        }
    }

    // =====================================================
    // PERMANENT DELETE
    // =====================================================

    public static boolean permanentlyDelete(
            Student student) {

        String deleteRequirementsSql =
                "DELETE FROM requirements WHERE student_id = ?";
        String deleteRemarksSql =
                "DELETE FROM remarks WHERE student_id = ?";
        String deleteAttendanceSql =
                "DELETE FROM student_attendance WHERE student_id = ?";
        String deleteActivitiesSql =
                "DELETE FROM student_activities WHERE student_id = ?";
        String deleteArchiveSql =
                "DELETE FROM archived_students WHERE student_id = ?";
        String deleteStudentSql =
                "DELETE FROM students "
                + "WHERE student_id = ? AND status = 'ARCHIVED'";

        try (Connection conn = DBConnection.getConnection()) {
            ensureArchiveTable(conn);
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                deleteByStudentID(conn, deleteRequirementsSql,
                        student.getStudentID());
                deleteByStudentID(conn, deleteRemarksSql,
                        student.getStudentID());
                deleteByStudentID(conn, deleteAttendanceSql,
                        student.getStudentID());
                deleteByStudentID(conn, deleteActivitiesSql,
                        student.getStudentID());
                deleteByStudentID(conn, deleteArchiveSql,
                        student.getStudentID());

                int deleted;
                try (PreparedStatement stmt =
                             conn.prepareStatement(deleteStudentSql)) {
                    stmt.setString(1, student.getStudentID());
                    deleted = stmt.executeUpdate();
                }

                if (deleted != 1) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }

        } catch (SQLException e) {

            showError(e);

            return false;
        }
    }

    // =====================================================
    // ADD REMARK
    // =====================================================

    public static boolean addRemark(
            String studentID,
            String message,
            String addedBy) {

        try (Connection conn = DBConnection.getConnection()) {
            addRemark(conn, studentID, message, addedBy);
            return true;
        } catch (SQLException e) {
            showError(e);
            return false;
        }
    }

    private static void addRemark(
            Connection conn,
            String studentID,
            String message,
            String addedBy) throws SQLException {

        String sql =
                "INSERT INTO remarks "
                + "(student_id, remark, added_by) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentID);
            stmt.setString(2, message);
            stmt.setString(3, addedBy);
            stmt.executeUpdate();
        }
    }

    // =====================================================
    // SEARCH STUDENTS
    // =====================================================

    public static List<Student>
    searchStudents(String keyword) {
        return searchStudents(keyword, "ACTIVE");
    }

    public static List<Student>
    searchStudents(String keyword, String status) {

        List<Student> students =
                new ArrayList<>();

        boolean allStatuses = "ALL".equals(status);
        String sql =
                "SELECT * FROM students "
                + (allStatuses ? "WHERE " : "WHERE status = ? AND ")
                + "(student_id LIKE ? "
                + "OR name LIKE ? "
                + "OR course LIKE ?) "
                + "ORDER BY name";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            String search =
                    "%" + keyword + "%";

            int parameter = 1;
            if (!allStatuses) {
                stmt.setString(parameter++, status);
            }

            stmt.setString(parameter++, search);
            stmt.setString(parameter++, search);
            stmt.setString(parameter, search);

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                students.add(
                        createStudent(rs)
                );
            }

        } catch (SQLException e) {

            showError(e);
        }

        return students;
    }

    // =====================================================
    // RESET STUDENT PASSWORD
    // =====================================================

    public static boolean resetStudentPassword(
            String studentID,
            String newPassword) {

        if (!showPasswordValidationError(newPassword)) {
            return false;
        }

        String sql =
                "UPDATE students "
                + "SET password = ? "
                + "WHERE student_id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, newPassword);
            stmt.setString(2, studentID);

            stmt.executeUpdate();

            return true;

        } catch (SQLException e) {

            showError(e);

            return false;
        }
    }

    // =====================================================
    // CHANGE STUDENT PASSWORD
    // =====================================================

    public static boolean changeStudentPassword(
            String studentID,
            String oldPassword,
            String newPassword) {

        if (!showPasswordValidationError(newPassword)) {
            return false;
        }

        String sql =
                "UPDATE students "
                + "SET password = ? "
                + "WHERE student_id = ? "
                + "AND password = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, newPassword);
            stmt.setString(2, studentID);
            stmt.setString(3, oldPassword);

            int updated =
                    stmt.executeUpdate();

            return updated > 0;

        } catch (SQLException e) {

            showError(e);

            return false;
        }
    }

    // =====================================================
    // GET DASHBOARD COUNT
    // =====================================================

    public static int getStudentCount(
            String status) {

        String sql =
                "SELECT COUNT(*) FROM students "
                + "WHERE status = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, status);

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {

            showError(e);
        }

        return 0;
    }

    private static void ensureArchiveTable(
            Connection conn) throws SQLException {

        String createSql =
                "CREATE TABLE IF NOT EXISTS archived_students ("
                + "student_id VARCHAR(255) NOT NULL PRIMARY KEY, "
                + "archived_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "archive_reason TEXT NOT NULL, "
                + "archived_by VARCHAR(255) NOT NULL"
                + ")";

        try (PreparedStatement stmt =
                     conn.prepareStatement(createSql)) {
            stmt.executeUpdate();
        }

        String migrateSql =
                "INSERT IGNORE INTO archived_students "
                + "(student_id, archive_reason, archived_by) "
                + "SELECT student_id, 'Previously archived', "
                + "'System migration' FROM students "
                + "WHERE status = 'ARCHIVED'";

        try (PreparedStatement stmt =
                     conn.prepareStatement(migrateSql)) {
            stmt.executeUpdate();
        }
    }

    private static void deleteByStudentID(
            Connection conn,
            String sql,
            String studentID) throws SQLException {

        try (PreparedStatement stmt =
                     conn.prepareStatement(sql)) {
            stmt.setString(1, studentID);
            stmt.executeUpdate();
        }
    }

    private static boolean showPasswordValidationError(
            String password) {

        String error =
                PasswordPolicy.validationError(password);

        if (error == null) {
            return true;
        }

        JOptionPane.showMessageDialog(
                null,
                error,
                "Invalid Password",
                JOptionPane.ERROR_MESSAGE
        );
        return false;
    }

    // =====================================================
    // CREATE STUDENT OBJECT
    // =====================================================

    private static Student createStudent(
            ResultSet rs)
            throws SQLException {

        Student student =
                new Student(
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("course"),
                        rs.getString("year_level"),
                        rs.getString("password")
                );

        student.setStatus(
                rs.getString("status")
        );
        student.setSection(rs.getString("section"));

        loadRequirements(student);
        loadRemarks(student);

        return student;
    }

    private static void ensureSectionColumn() {
        try (Connection conn = DBConnection.getConnection()) {
            boolean lockAcquired = false;
            try {
                acquireStudentCreationLock(conn);
                lockAcquired = true;

                boolean sectionExists = false;
                try (ResultSet columns = conn.getMetaData().getColumns(
                        conn.getCatalog(),
                        null,
                        "students",
                        "section"
                )) {
                    sectionExists = columns.next();
                }
                if (!sectionExists) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "ALTER TABLE students ADD COLUMN "
                                    + "section VARCHAR(100) NOT NULL DEFAULT ''"
                    )) {
                        stmt.executeUpdate();
                    }
                }

                conn.setAutoCommit(false);
                backfillStudentSections(conn);
                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                if (lockAcquired) {
                    releaseStudentCreationLock(conn);
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private static void normalizeCatalogCourses() {
        String selectSql = "SELECT student_id, course FROM students";
        List<String[]> normalizedCourses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement select = conn.prepareStatement(selectSql);
                 ResultSet rs = select.executeQuery()) {
                while (rs.next()) {
                    String storedCourse = rs.getString("course");
                    String canonical = CourseCatalog.canonicalCode(storedCourse);
                    if (canonical != null && !canonical.equals(storedCourse)) {
                        normalizedCourses.add(new String[]{
                                canonical,
                                rs.getString("student_id")
                        });
                    }
                }
            }
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE students SET course = ? WHERE student_id = ?"
            )) {
                for (String[] normalized : normalizedCourses) {
                    update.setString(1, normalized[0]);
                    update.setString(2, normalized[1]);
                    update.addBatch();
                }
                update.executeBatch();
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private static void normalizeAndRebalanceSections() {
        String sql =
                "SELECT student_id, course, year_level "
                + "FROM students WHERE status <> 'ARCHIVED' "
                + "ORDER BY student_id";

        List<String[]> cohortStudents = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            boolean lockAcquired = false;
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                acquireStudentCreationLock(conn);
                lockAcquired = true;
                conn.setAutoCommit(false);
                try (PreparedStatement select = conn.prepareStatement(sql);
                     ResultSet rs = select.executeQuery()) {
                    while (rs.next()) {
                        String canonicalCourse =
                                CourseCatalog.canonicalCode(
                                        rs.getString("course")
                                );
                        String canonicalYear =
                                canonicalYearLevel(
                                        rs.getString("year_level")
                                );
                        if (canonicalCourse != null && canonicalYear != null) {
                            cohortStudents.add(new String[]{
                                    rs.getString("student_id"),
                                    canonicalCourse,
                                    canonicalYear
                            });
                        }
                    }
                }

                Map<String, Integer> cohortCounts = new HashMap<>();
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE students SET course = ?, year_level = ?, "
                                + "section = ? WHERE student_id = ?"
                )) {
                    for (String[] student : cohortStudents) {
                        String cohortKey = student[1] + "\u0000" + student[2];
                        int studentNumber =
                                cohortCounts.getOrDefault(cohortKey, 0);
                        update.setString(1, student[1]);
                        update.setString(2, student[2]);
                        update.setString(
                                3,
                                sectionName(
                                        student[1],
                                        student[2],
                                        studentNumber / 50
                                )
                        );
                        update.setString(4, student[0]);
                        update.addBatch();
                        cohortCounts.put(cohortKey, studentNumber + 1);
                    }
                    update.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
                if (lockAcquired) {
                    releaseStudentCreationLock(conn);
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private static String canonicalYearLevel(String yearLevel) {
        if (yearLevel == null) {
            return null;
        }

        String normalized = yearLevel.trim().toLowerCase(Locale.ROOT);
        String digits = normalized.replaceAll("[^0-9]", "");
        if (digits.matches("[1-4]")) {
            switch (digits) {
                case "1":
                    return "1st Year";
                case "2":
                    return "2nd Year";
                case "3":
                    return "3rd Year";
                case "4":
                    return "4th Year";
                default:
                    break;
            }
        }

        switch (normalized.replaceAll("[^a-z]", "")) {
            case "firstyear":
            case "freshman":
                return "1st Year";
            case "secondyear":
            case "sophomore":
                return "2nd Year";
            case "thirdyear":
            case "junior":
                return "3rd Year";
            case "fourthyear":
            case "senior":
                return "4th Year";
            default:
                return null;
        }
    }

    private static int yearNumber(String yearLevel) {
        String canonical = canonicalYearLevel(yearLevel);
        if (canonical == null) {
            return 0;
        }
        return Integer.parseInt(
                canonical.replaceAll("[^0-9]", "")
        );
    }

    private static String currentAcademicYearCode() {
        LocalDate today = LocalDate.now();
        int academicStartYear = today.getMonthValue() >= 6
                ? today.getYear()
                : today.getYear() - 1;
        int academicEndYear = academicStartYear + 1;
        return String.format(
                Locale.ROOT,
                "%02d%02d",
                academicStartYear % 100,
                academicEndYear % 100
        );
    }

    private static void backfillStudentSections(
            Connection conn) throws SQLException {

        String selectSql =
                "SELECT student_id, course, year_level FROM students "
                + "WHERE course IS NOT NULL AND course <> '' "
                + "AND year_level IS NOT NULL AND year_level <> '' "
                + "AND (section IS NULL OR section = '') "
                + "ORDER BY course, year_level, student_id";

        List<String[]> unassignedStudents = new ArrayList<>();
        try (PreparedStatement select = conn.prepareStatement(selectSql);
             ResultSet students = select.executeQuery()) {
            while (students.next()) {
                unassignedStudents.add(new String[]{
                        students.getString("student_id"),
                        students.getString("course"),
                        students.getString("year_level")
                });
            }
        }

        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE students SET section = ? WHERE student_id = ?"
        )) {
            for (String[] student : unassignedStudents) {
                String section = findAvailableSection(
                        conn,
                        student[1],
                        student[2],
                        student[0]
                );
                update.setString(1, section);
                update.setString(2, student[0]);
                update.executeUpdate();
            }
        }
    }

    private static void acquireStudentCreationLock(
            Connection conn) throws SQLException {

        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT GET_LOCK(?, 10)"
        )) {
            stmt.setString(1, STUDENT_CREATION_LOCK);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) {
                    throw new SQLException(
                            "Could not obtain the lock for creating or assigning a student."
                    );
                }
            }
        }
    }

    private static void releaseStudentCreationLock(
            Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(
                     "SELECT RELEASE_LOCK(?)"
             )) {
            stmt.setString(1, STUDENT_CREATION_LOCK);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getObject(1) == null) {
                    throw new SQLException(
                            "The student assignment lock was not released."
                    );
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private static String generateStudentID(
            Connection conn,
            String yearLevel) throws SQLException {

        BigInteger maxID = BigInteger.ZERO;
        try (
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT student_id FROM students"
                );
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                String value = rs.getString("student_id");
                if (value != null && value.matches("\\d{2}-\\d{4}-\\d{6}")) {
                    BigInteger numericID = new BigInteger(
                            value.substring(value.lastIndexOf('-') + 1)
                    );
                    if (numericID.compareTo(maxID) > 0) {
                        maxID = numericID;
                    }
                }
            }
        }

        BigInteger nextID = maxID.add(BigInteger.ONE);
        if (nextID.compareTo(BigInteger.valueOf(999999)) > 0) {
            throw new SQLException(
                    "The six-digit student ID sequence has reached its limit."
            );
        }
        return formatStudentID(
                yearLevel,
                currentAcademicYearCode(),
                nextID
        );
    }

    private static String formatStudentID(
            String yearLevel,
            String academicYear,
            BigInteger sequence) throws SQLException {

        int yearNumber = yearNumber(yearLevel);
        if (yearNumber < 0 || yearNumber > 4) {
            throw new SQLException("Invalid year level for student ID.");
        }
        if (sequence.signum() < 1
                || sequence.compareTo(BigInteger.valueOf(999999)) > 0) {
            throw new SQLException(
                    "The six-digit student ID sequence has reached its limit."
            );
        }

        return String.format(
                Locale.ROOT,
                "%02d-%s-%06d",
                yearNumber,
                academicYear,
                sequence
        );
    }

    private static void migrateStudentIDs() {
        String createMigrationTableSql =
                "CREATE TABLE IF NOT EXISTS student_management_migrations ("
                + "migration_key VARCHAR(100) NOT NULL PRIMARY KEY, "
                + "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")";
        String selectSql =
                "SELECT student_id, section, year_level FROM students "
                + "ORDER BY course, year_level, section, student_id";
        List<String[]> mappings = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            boolean lockAcquired = false;
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                acquireStudentCreationLock(conn);
                lockAcquired = true;
                try (PreparedStatement createMigrationTable =
                             conn.prepareStatement(createMigrationTableSql)) {
                    createMigrationTable.executeUpdate();
                }
                ensureArchiveTable(conn);
                conn.setAutoCommit(false);

                try (PreparedStatement migrationCheck =
                             conn.prepareStatement(
                                     "SELECT migration_key "
                                             + "FROM student_management_migrations "
                                             + "WHERE migration_key = ?"
                             )) {
                    migrationCheck.setString(1, "student-id-format-v2");
                    try (ResultSet rs = migrationCheck.executeQuery()) {
                        if (rs.next()) {
                            return;
                        }
                    }
                }

                try (PreparedStatement select =
                             conn.prepareStatement(selectSql);
                     ResultSet rs = select.executeQuery()) {
                    BigInteger sequence = BigInteger.ONE;
                    while (rs.next()) {
                        String oldID = rs.getString("student_id");
                        String temporaryID =
                                "__STUDENT_ID_MIGRATION_"
                                        + sequence + "__";
                        String newID = formatStudentID(
                                rs.getString("year_level"),
                                currentAcademicYearCode(),
                                sequence
                        );
                        mappings.add(new String[]{
                                oldID,
                                temporaryID,
                                newID
                        });
                        sequence = sequence.add(BigInteger.ONE);
                    }
                }

                try (PreparedStatement disableForeignKeys =
                             conn.prepareStatement(
                                     "SET FOREIGN_KEY_CHECKS = 0"
                             )) {
                    disableForeignKeys.execute();
                }
                try {
                    updateStudentIDReferences(conn, mappings, 1);
                    updateStudentIDs(conn, mappings, 1);
                    updateStudentIDReferences(conn, mappings, 2);
                    updateStudentIDs(conn, mappings, 2);
                    try (PreparedStatement markMigration =
                                 conn.prepareStatement(
                                         "INSERT INTO student_management_migrations "
                                                 + "(migration_key) VALUES (?)"
                                 )) {
                        markMigration.setString(1, "student-id-format-v2");
                        markMigration.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    try (PreparedStatement enableForeignKeys =
                                 conn.prepareStatement(
                                         "SET FOREIGN_KEY_CHECKS = 1"
                                 )) {
                        enableForeignKeys.execute();
                    }
                }
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
                if (lockAcquired) {
                    releaseStudentCreationLock(conn);
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private static void updateStudentIDReferences(
            Connection conn,
            List<String[]> mappings,
            int targetIndex) throws SQLException {

        String[] tableNames = {
                "requirements",
                "remarks",
                "archived_students"
        };
        for (String table : tableNames) {
            if (!tableExists(conn, table)) {
                continue;
            }
            String sql =
                    "UPDATE `" + table + "` SET student_id = ? "
                    + "WHERE student_id = ?";
            try (PreparedStatement update = conn.prepareStatement(sql)) {
                for (String[] mapping : mappings) {
                    update.setString(1, mapping[targetIndex]);
                    update.setString(2, mapping[targetIndex == 1 ? 0 : 1]);
                    update.addBatch();
                }
                update.executeBatch();
            }
        }
    }

    private static void updateStudentIDs(
            Connection conn,
            List<String[]> mappings,
            int targetIndex) throws SQLException {

        String sql =
                "UPDATE students SET student_id = ? WHERE student_id = ?";
        try (PreparedStatement update = conn.prepareStatement(sql)) {
            for (String[] mapping : mappings) {
                update.setString(1, mapping[targetIndex]);
                update.setString(2, mapping[targetIndex == 1 ? 0 : 1]);
                update.addBatch();
            }
            update.executeBatch();
        }
    }

    private static boolean tableExists(
            Connection conn,
            String tableName) throws SQLException {

        try (ResultSet tables = conn.getMetaData().getTables(
                conn.getCatalog(),
                null,
                tableName,
                new String[]{"TABLE"}
        )) {
            return tables.next();
        }
    }

    private static String findAvailableSection(
            Connection conn,
            String course,
            String yearLevel,
            String excludedStudentID) throws SQLException {

        String sql =
                "SELECT section, COUNT(*) AS student_count "
                + "FROM students WHERE course = ? AND year_level = ? "
                + "AND section <> '' "
                + "AND status <> 'ARCHIVED' "
                + (excludedStudentID == null ? "" : "AND student_id <> ? ")
                + "GROUP BY section";

        Map<String, Integer> sectionCounts = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course);
            stmt.setString(2, yearLevel);
            if (excludedStudentID != null) {
                stmt.setString(3, excludedStudentID);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sectionCounts.put(
                            rs.getString("section"),
                            rs.getInt("student_count")
                    );
                }
            }
        }

        for (int sectionNumber = 0; ; sectionNumber++) {
            String candidate =
                    sectionName(course, yearLevel, sectionNumber);
            if (sectionCounts.getOrDefault(candidate, 0) < 50) {
                return candidate;
            }
        }
    }

    private static String sectionName(
            String course,
            String yearLevel,
            int sectionNumber) {

        String yearCode = yearLevel.replaceAll("[^0-9]", "");
        if (yearCode.isEmpty()) {
            yearCode = yearLevel.trim().replaceAll("\\s+", "");
        }
        return course.trim().toUpperCase(Locale.ROOT)
                + "-" + yearCode + "-"
                + sectionSuffix(sectionNumber);
    }

    private static String sectionSuffix(int sectionNumber) {
        StringBuilder suffix = new StringBuilder();
        int value = sectionNumber;
        do {
            suffix.insert(0, (char) ('A' + value % 26));
            value = value / 26 - 1;
        } while (value >= 0);
        return suffix.toString();
    }

    // =====================================================
    // LOAD REQUIREMENTS
    // =====================================================

    private static void loadRequirements(
            Student student) {

        String sql =
                "SELECT * FROM requirements "
                + "WHERE student_id = ?";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    student.getStudentID()
            );

            ResultSet rs =
                    stmt.executeQuery();

            if (rs.next()) {

                student.setInformationSubmitted(
                        rs.getBoolean(
                                "information_submitted"
                        )
                );

                student.setEnrollmentCompleted(
                        rs.getBoolean(
                                "enrollment_completed"
                        )
                );

                student.setActivity1Completed(
                        rs.getBoolean(
                                "activity1_completed"
                        )
                );

                student.setActivity2Completed(
                        rs.getBoolean(
                                "activity2_completed"
                        )
                );
            }

        } catch (SQLException e) {

            showError(e);
        }
    }

    // =====================================================
    // LOAD REMARKS
    // =====================================================

    private static void loadRemarks(
            Student student) {

        String sql =
                "SELECT remark, added_by, date_added "
                + "FROM remarks "
                + "WHERE student_id = ? "
                + "ORDER BY date_added DESC";

        try (
                Connection conn =
                        DBConnection.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    student.getStudentID()
            );

            ResultSet rs =
                    stmt.executeQuery();

            while (rs.next()) {

                Timestamp timestamp =
                        rs.getTimestamp("date_added");

                student.addRemark(
                        new Remark(
                                rs.getString("remark"),
                                rs.getString("added_by"),
                                timestamp.toLocalDateTime()
                        )
                );
            }

        } catch (SQLException e) {

            showError(e);
        }
    }

    // =====================================================
    // DATABASE ERROR
    // =====================================================

    private static void showError(
            SQLException e) {

        e.printStackTrace();

        JOptionPane.showMessageDialog(
                null,
                "MySQL Database Error:\n\n"
                        + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
