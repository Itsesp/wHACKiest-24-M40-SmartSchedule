package com.example.smartschedule.manager;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClubManager {

    // Data model class for Club Event
    public static class ClubEvent {
        private String clubName;
        private String eventName;
        private String date;

        public ClubEvent(String clubName, String eventName, String date) {
            this.clubName = clubName;
            this.eventName = eventName;
            this.date = date;
        }

        public String getClubName() {
            return clubName;
        }

        public String getEventName() {
            return eventName;
        }

        public String getDate() {
            return date;
        }
    }

    // Fetch and update club events from Firestore
    public void fetchAndUpdateClubEvents(Context context) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Clubs")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ClubEvent> fetchedClubEvents = new ArrayList<>();

                    for (QueryDocumentSnapshot clubDoc : queryDocumentSnapshots) {
                        String clubName = clubDoc.getId();
                        CollectionReference eventsRef = firestore.collection("Clubs")
                                .document(clubName)
                                .collection("events");

                        eventsRef.get()
                                .addOnSuccessListener(eventData -> {
                                    if (!eventData.isEmpty()) {
                                        for (QueryDocumentSnapshot eventDoc : eventData) {
                                            String eventName = eventDoc.getString("event");
                                            Timestamp timestamp = eventDoc.getTimestamp("date");

                                            if (timestamp != null) {
                                                Date date = timestamp.toDate();
                                                String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                                                fetchedClubEvents.add(new ClubEvent(clubName, eventName, formattedDate));
                                            }
                                        }
                                    }

                                    updateLocalClubEvents(context, fetchedClubEvents);
                                })
                                .addOnFailureListener(e -> {

                                    e.printStackTrace();
                                    updateLocalClubEvents(context, fetchedClubEvents);
                                });
                    }
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void updateLocalClubEvents(Context context, List<ClubEvent> fetchedClubEvents) {
        List<ClubEvent> localClubEvents = loadClubEventsLocally(context);

        for (ClubEvent fetchedEvent : fetchedClubEvents) {
            boolean exists = false;

            for (ClubEvent localEvent : localClubEvents) {
                if (localEvent.getClubName().equals(fetchedEvent.getClubName()) && localEvent.getEventName().equals(fetchedEvent.getEventName())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                localClubEvents.add(fetchedEvent);
            }
        }

        saveClubEventsLocally(context, localClubEvents);
    }

    private void saveClubEventsLocally(Context context, List<ClubEvent> clubEventList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ClubEventData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(clubEventList);
        editor.putString("club_events", json);
        editor.apply();
    }

    public List<ClubEvent> loadClubEventsLocally(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("ClubEventData", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("club_events", null);
        Gson gson = new Gson();

        Type type = new TypeToken<List<ClubEvent>>() {}.getType();
        return json != null ? gson.fromJson(json, type) : new ArrayList<>();
    }
}
