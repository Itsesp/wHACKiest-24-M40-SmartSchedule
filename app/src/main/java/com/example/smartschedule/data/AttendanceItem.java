package com.example.smartschedule.data;

public class AttendanceItem {
    private String subjectName;
    private int percentage;

    public AttendanceItem(String subjectName, int percentage) {
        this.subjectName = subjectName;
        this.percentage = percentage;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public int getPercentage() {
        return percentage;
    }
}
