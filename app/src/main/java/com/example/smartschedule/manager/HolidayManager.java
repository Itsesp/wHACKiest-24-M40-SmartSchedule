package com.example.smartschedule.manager;

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

    public static class Holiday {
        private String name;
        private String date;
        private Timestamp time;

        public Holiday(String name, String date , Timestamp time) {
            this.name = name;
            this.date = date;
            this.time=time;
        }

        public String getName() {
            return name;
        }

        public String getDate() {
            return date;
        }
        public Timestamp getTime() {return time;}
    }

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
                            fetchedHolidays.add(new Holiday(holidayName, formattedDate,timestamp));
                        }
                    }

                    updateLocalHolidays(context, fetchedHolidays);
                })
                .addOnFailureListener(e -> e.printStackTrace());
    }

    private void updateLocalHolidays(Context context, List<Holiday> fetchedHolidays) {
        List<Holiday> localHolidays = loadHolidaysLocally(context);

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

        saveHolidaysLocally(context, localHolidays);
    }

    private void saveHolidaysLocally(Context context, List<Holiday> holidayList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("HolidayData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        String json = gson.toJson(holidayList);
        editor.putString("holidays", json);
        editor.apply();
    }
    public List<Holiday> loadHolidaysLocally(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("HolidayData", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("holidays", null);
        Gson gson = new Gson();

        Type type = new TypeToken<List<Holiday>>() {}.getType();
        return json != null ? gson.fromJson(json, type) : new ArrayList<>();
    }
    public String loadHolidayName(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("HolidayData", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("holidays", null);
        return json;

    }

}
