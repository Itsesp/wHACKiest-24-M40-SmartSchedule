package com.example.smartschedule.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timetable")
public class TimetableEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String day;
    public String subjectName;
    public String startTime;
    public String endTime;

    // Constructor
    public TimetableEntry(String day, String subjectName, String startTime, String endTime) {
        this.day = day;
        this.subjectName = subjectName;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

