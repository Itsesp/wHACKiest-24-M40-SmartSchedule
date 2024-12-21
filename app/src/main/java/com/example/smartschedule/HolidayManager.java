package com.example.smartschedule;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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

public class HolidayManager {

    // Data model class
    public static class Holiday {
        private String name;
        private String date;

        public Holiday(String name, String date) {
            this.name = name;
            this.date = date;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }
    }

    // Fetch and update holidays from Firestore
    public void fetchAndUpdateHolidays(Context context) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Holidays")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Holiday> fetchedHolidays = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String holidayName = document.getId();
                        Timestamp timestamp = document.getTimestamp("date");

                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            String formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                            fetchedHolidays.add(new Holiday(holidayName, formattedDate));
                        }
                    }

                    updateLocalHolidays(context, fetchedHolidays);
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    // Update local holidays
    private void updateLocalHolidays(Context context, List<Holiday> fetchedHolidays) {
        List<Holiday> localHolidays = loadHolidaysLocally(context);

        // Add only new holidays
        for (Holiday fetchedHoliday : fetchedHolidays) {
            boolean exists = false;

            for (Holiday localHoliday : localHolidays) {
                if (localHoliday.getName().equals(fetchedHoliday.getName())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                localHolidays.add(fetchedHoliday);
            }
        }

        // Save updated list locally
        saveHolidaysLocally(context, localHolidays);
    }

    // Save holidays locally
    private void saveHolidaysLocally(Context context, List<Holiday> holidayList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("HolidayData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        // Convert List<Holiday> to JSON
        String json = gson.toJson(holidayList);
        editor.putString("holidays", json);
        editor.apply();
    }

    // Load holidays from local storage
    public List<Holiday> loadHolidaysLocally(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("HolidayData", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("holidays", null);
        Gson gson = new Gson();

        // Convert JSON back to List<Holiday>
        Type type = new TypeToken<List<Holiday>>() {}.getType();
        return json != null ? gson.fromJson(json, type) : new ArrayList<>();
    }
}
