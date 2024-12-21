package com.example.smartschedule.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.example.smartschedule.HolidayManager;
import com.example.smartschedule.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private KalendarView mKalendarView;
    private HolidayManager holidayManager;
    private List<Map<String, Object>> eventsList;
    private FirebaseFirestore db;
    private static final String TAG = "HomeFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();
        eventsList = new ArrayList<>();

        fetchAllEvents();
        mKalendarView = view.findViewById(R.id.kalendar);
        holidayManager = new HolidayManager();
        holidayManager.fetchAndUpdateHolidays(requireContext());
        setHolidayColors();
        mKalendarView.setInitialSelectedDate(new Date());
        return view;
    }

    private void setHolidayColors() {

        List<HolidayManager.Holiday> holidays = holidayManager.loadHolidaysLocally(requireContext());
        List<ColoredDate> datesColors = new ArrayList<>();
        List<EventObjects> events = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (HolidayManager.Holiday holiday : holidays) {
            try {
                Date holidayDate = sdf.parse(holiday.getDate());
                if (holidayDate != null) {
                    events.add(new EventObjects(holiday.getName(),holidayDate));
                    datesColors.add(new ColoredDate(holidayDate, getResources().getColor(R.color.holiday_red)));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        mKalendarView.setEvents(events);
        mKalendarView.setColoredDates(datesColors);
    }
    private void fetchAllEvents() {
        Log.d(TAG, "Fetching all events...");
        fetchHolidays();
    }

    private void fetchHolidays() {
        Log.d(TAG, "Fetching holidays...");
        db.collection("Holidays")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Map<String, Object> event = new HashMap<>();
                            event.put("type", "Holiday");
                            event.put("name", doc.getId());
                            event.put("date", doc.getTimestamp("date"));
                            eventsList.add(event);
                        }
                        fetchExams();
                    } else {
                        Log.e(TAG, "Failed to fetch Holidays: ", task.getException());
                    }
                });
    }

    private void fetchExams() {
        Log.d(TAG, "Fetching exams...");
        db.collection("Exams")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot examDoc : task.getResult()) {
                            String examName = examDoc.getId();

                            db.collection("Exams")
                                    .document(examName)
                                    .collection("dates")
                                    .get()
                                    .addOnCompleteListener(datesTask -> {
                                        if (datesTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot dateDoc : datesTask.getResult()) {
                                                Map<String, Object> event = new HashMap<>();
                                                event.put("type", "Exam");
                                                event.put("exam", examName);
                                                event.put("id", dateDoc.getId());
                                                event.put("date", dateDoc.getTimestamp("date"));
                                                eventsList.add(event);
                                            }
                                            Log.d(TAG, "Fetched exam dates for: " + examName);
                                        } else {
                                            Log.e(TAG, "Failed to fetch exam dates: ", datesTask.getException());
                                        }
                                    });
                        }
                        fetchClubEvents();
                    } else {
                        Log.e(TAG, "Failed to fetch Exams: ", task.getException());
                    }
                });
    }

    private void fetchClubEvents() {
        Log.d(TAG, "Fetching club events...");
        db.collection("Clubs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot clubDoc : task.getResult()) {
                            String clubName = clubDoc.getId();

                            db.collection("Clubs")
                                    .document(clubName)
                                    .collection("events")
                                    .get()
                                    .addOnCompleteListener(eventsTask -> {
                                        if (eventsTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot eventDoc : eventsTask.getResult()) {
                                                Map<String, Object> event = new HashMap<>();
                                                event.put("type", "Club");
                                                event.put("club", clubName);
                                                event.put("name", eventDoc.getId());
                                                event.put("date", eventDoc.getTimestamp("date"));
                                                eventsList.add(event);
                                            }
                                            Log.d(TAG, "Fetched club events for: " + clubName);
                                        } else {
                                            Log.e(TAG, "Failed to fetch club events: ", eventsTask.getException());
                                        }
                                    });
                        }
                        displayEvents();
                    } else {
                        Log.e(TAG, "Failed to fetch Clubs: ", task.getException());
                    }
                });
    }

    private void displayEvents() {
        Log.d(TAG, "Displaying events...");
        // Sort events by date
        Collections.sort(eventsList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Timestamp date1 = (Timestamp) o1.get("date");
                Timestamp date2 = (Timestamp) o2.get("date");
                if (date1 == null || date2 == null) {
                    Log.e(TAG, "Null timestamp found during sorting: " + o1 + " or " + o2);
                    return 0;
                }
                return date1.toDate().compareTo(date2.toDate());
            }
        });

        // Log sorted events
        for (Map<String, Object> event : eventsList) {
            Log.d(TAG, "Event: " + event);
        }

        // TODO: Update the UI with sorted events
    }
}