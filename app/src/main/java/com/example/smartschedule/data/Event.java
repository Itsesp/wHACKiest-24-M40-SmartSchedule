package com.example.smartschedule.data;


import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String eventName;
    private Timestamp eventDate;
    private String eventDescription;

    public Event(String eventName, Timestamp eventDate, String eventDescription) {
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventDescription = eventDescription;
    }

    public String getEventName() {
        return eventName;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }
    public String getEventDescription() {
        return eventDescription;
    }

    public String getFormattedEventDate() {
        if (eventDate != null) {
            Date date = eventDate.toDate(); // Convert Timestamp to Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.format(date);
        } else {
            return "";
        }
    }
}
