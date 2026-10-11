CREATE DATABASE IF NOT EXISTS student_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_management;

CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    course VARCHAR(100) NOT NULL,
    year_level VARCHAR(100) NOT NULL,
    section VARCHAR(100) NOT NULL DEFAULT '',
    password VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teachers (
    username VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS requirements (
    student_id VARCHAR(255) NOT NULL,
    information_submitted BOOLEAN NOT NULL DEFAULT FALSE,
    enrollment_completed BOOLEAN NOT NULL DEFAULT FALSE,
    activity1_completed BOOLEAN NOT NULL DEFAULT FALSE,
    activity2_completed BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS remarks (
    remark_id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(255) NOT NULL,
    remark TEXT NOT NULL,
    added_by VARCHAR(255) NOT NULL,
    date_added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (remark_id),
    INDEX idx_remarks_student_date (student_id, date_added)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
