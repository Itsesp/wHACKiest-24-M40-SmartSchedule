package com.example.smartschedule.data;

import java.util.List;

public class Club {
    private String name;
    private String description;
    private List<Event> events;
    private String url;

    public Club(String name, String description,String url, List<Event> events) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.events = events;
    }

    public String getName() {
        return name;
    }
    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Event getUpcomingEvent() {
        if (events != null && !events.isEmpty()) {
            // Sort events by date and return the next upcoming event
            Event upcomingEvent = null;
            long currentTime = System.currentTimeMillis(); // Get current time in milliseconds
            for (Event event : events) {
                if (event.getEventDate() != null) {
                    long eventTime = event.getEventDate().toDate().getTime(); // Convert Timestamp to Date and get time
                    if (eventTime > currentTime) { // Check if the event is upcoming
                        if (upcomingEvent == null || eventTime < upcomingEvent.getEventDate().toDate().getTime()) {
                            upcomingEvent = event;
                        }
                    }
                }
            }
            return upcomingEvent;
        }
        return null;
    }

}
