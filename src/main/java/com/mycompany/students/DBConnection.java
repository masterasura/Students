package com.mycompany.students;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL = System.getenv().getOrDefault(
            "STUDENTS_DB_URL",
            "jdbc:mysql://localhost:3306/student_management"
                    + "?useSSL=false"
                    + "&serverTimezone=Asia/Manila"
                    + "&allowPublicKeyRetrieval=true"
    );
    private static final String USER =
            System.getenv().getOrDefault("STUDENTS_DB_USER", "root");
    private static final String PASSWORD =
            System.getenv().getOrDefault("STUDENTS_DB_PASSWORD", "");

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}
