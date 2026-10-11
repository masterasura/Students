package com.mycompany.students;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Remark {

    private String message;
    private String addedBy;
    private LocalDateTime dateTime;

    public Remark(
            String message,
            String addedBy) {

        this.message = message;
        this.addedBy = addedBy;
        this.dateTime =
                LocalDateTime.now();
    }

    public Remark(
            String message,
            String addedBy,
            LocalDateTime dateTime) {

        this.message = message;
        this.addedBy = addedBy;
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getFormattedDate() {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "MMM dd, yyyy - hh:mm a"
                );

        return dateTime.format(formatter);
    }

    @Override
    public String toString() {

        return "[" + getFormattedDate() + "] "
                + addedBy
                + ": "
                + message;
    }
}
