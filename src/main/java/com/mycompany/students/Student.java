package com.mycompany.students;


import java.util.ArrayList;
import java.util.List;

public class Student {

    private String studentID;
    private String name;
    private String course;
    private String yearLevel;
    private String section;
    private String password;
    private String status;

    private boolean informationSubmitted;
    private boolean enrollmentCompleted;
    private boolean activity1Completed;
    private boolean activity2Completed;

    private List<Remark> remarks;

    public Student(
            String studentID,
            String name,
            String course,
            String yearLevel,
            String password) {

        this.studentID = studentID;
        this.name = name;
        this.course = course;
        this.yearLevel = yearLevel;
        this.section = "";
        this.password = password;

        this.status = "ACTIVE";

        this.informationSubmitted = false;
        this.enrollmentCompleted = false;
        this.activity1Completed = false;
        this.activity2Completed = false;

        this.remarks = new ArrayList<>();
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isInformationSubmitted() {
        return informationSubmitted;
    }

    public void setInformationSubmitted(
            boolean informationSubmitted) {

        this.informationSubmitted =
                informationSubmitted;
    }

    public boolean isEnrollmentCompleted() {
        return enrollmentCompleted;
    }

    public void setEnrollmentCompleted(
            boolean enrollmentCompleted) {

        this.enrollmentCompleted =
                enrollmentCompleted;
    }

    public boolean isActivity1Completed() {
        return activity1Completed;
    }

    public void setActivity1Completed(
            boolean activity1Completed) {

        this.activity1Completed =
                activity1Completed;
    }

    public boolean isActivity2Completed() {
        return activity2Completed;
    }

    public void setActivity2Completed(
            boolean activity2Completed) {

        this.activity2Completed =
                activity2Completed;
    }

    public List<Remark> getRemarks() {
        return remarks;
    }

    public void addRemark(Remark remark) {
        remarks.add(remark);
    }

    public int getCompletedRequirements() {

        int count = 0;

        if (informationSubmitted) {
            count++;
        }

        if (enrollmentCompleted) {
            count++;
        }

        if (activity1Completed) {
            count++;
        }

        if (activity2Completed) {
            count++;
        }

        return count;
    }

    public int getTotalRequirements() {
        return 4;
    }

    public boolean isFullyCompliant() {
        return getCompletedRequirements() == 4;
    }

    @Override
    public String toString() {
        return studentID + " - " + name;
    }
}
