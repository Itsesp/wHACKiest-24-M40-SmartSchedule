package com.example.smartschedule.data;

import java.util.List;

public class DayData {
    private String dayName;
    private int dayOfMonth;
    private boolean isHoliday;
    private boolean isExamDay;
    private boolean isEventDay;
    private boolean isOtherEventDay;
    private List<String> events;

    // Constructor
    public DayData(String dayName, int dayOfMonth, boolean isHoliday, boolean isExamDay, boolean isEventDay, boolean isOtherEventDay,List<String> events) {
        this.dayName = dayName;
        this.dayOfMonth = dayOfMonth;
        this.isHoliday = isHoliday;
        this.isExamDay = isExamDay;
        this.isEventDay = isEventDay;
        this.isOtherEventDay = isOtherEventDay;
        this.events = events;
    }

    // Getters
    public String getDayName() {
        return dayName;
    }
    public List<String> getEvents() {
        return events;
    }
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public boolean isExamDay() {
        return isExamDay;
    }

    public boolean isEventDay() {
        return isEventDay;
    }

    public boolean isOtherEventDay() {
        return isOtherEventDay;
    }
}
