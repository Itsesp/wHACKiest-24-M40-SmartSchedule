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
import com.example.smartschedule.ClubManager;
import com.example.smartschedule.ExamManager;
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
    private ExamManager examManager;
    private List<Map<String, Object>> eventsList;
    private FirebaseFirestore db;
    private ClubManager clubManager;
    List<ColoredDate> examColors = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        db = FirebaseFirestore.getInstance();
        eventsList = new ArrayList<>();
        mKalendarView = view.findViewById(R.id.kalendar);
        holidayManager = new HolidayManager();
        clubManager = new ClubManager();
        examManager=new ExamManager();
        examManager.fetchAndUpdateExams(requireContext());
        holidayManager.fetchAndUpdateHolidays(requireContext());
        clubManager.fetchAndUpdateClubEvents(requireContext());
        setEventColors();
        mKalendarView.setInitialSelectedDate(new Date());
        return view;
    }

    private void setEventColors() {
        List<HolidayManager.Holiday> holidays = holidayManager.loadHolidaysLocally(requireContext());
        List<ExamManager.Exam> exams = examManager.loadExamsLocally(requireContext());
        List<ClubManager.ClubEvent> clubEvents = clubManager.loadClubEventsLocally(requireContext()); // Load club events

        List<ColoredDate> datesColors = new ArrayList<>();
        List<EventObjects> events = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (HolidayManager.Holiday holiday : holidays) {
            try {
                Date holidayDate = sdf.parse(holiday.getDate());
                if (holidayDate != null) {
                    events.add(new EventObjects(holiday.getName(), holidayDate));
                    datesColors.add(new ColoredDate(holidayDate, getResources().getColor(R.color.holiday_red))); // Color for holidays
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (ExamManager.Exam exam : exams) {
            try {
                Date examDate = sdf.parse(exam.getDate());
                if (examDate != null) {
                    events.add(new EventObjects(exam.getExamName(), examDate));
                    datesColors.add(new ColoredDate(examDate, getResources().getColor(R.color.purple_200))); // Color for exams
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (ClubManager.ClubEvent clubEvent : clubEvents) {
            try {
                Date clubEventDate = sdf.parse(clubEvent.getDate());
                if (clubEventDate != null) {
                    events.add(new EventObjects(clubEvent.getEventName(), clubEventDate));
                    datesColors.add(new ColoredDate(clubEventDate, getResources().getColor(R.color.purple_700))); // Color for club events
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        mKalendarView.setEvents(events);
        mKalendarView.setColoredDates(datesColors);
    }

    private void fetchHolidays() {
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
                    } else {
                        Log.e("FETCH_ERROR", "Failed to fetch Holidays", task.getException());
                    }
                    displayEvents();
                });
    }

    private void fetchExams() {
        db.collection("Exams")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot examDoc : task.getResult()) {
                            String examName = examDoc.getId();
                            Map<String, Object> examData = examDoc.getData();
                            for (Map.Entry<String, Object> entry : examData.entrySet()) {
                                if (entry.getValue() instanceof Timestamp) {
                                    Map<String, Object> event = new HashMap<>();
                                    event.put("type", "Exam");
                                    event.put("name", examName);
                                    event.put("date", entry.getValue());
                                    eventsList.add(event);
                                    Object value = entry.getValue();

                                    if (value instanceof Timestamp) {
                                        Timestamp timestamp = (Timestamp) value;
                                        Date date = timestamp.toDate();
                                        examColors.add(new ColoredDate(date, getResources().getColor(R.color.purple_200)));

                                    }

                                }
                            }
                        }
                    } else {
                        Log.e("FETCH_ERROR", "Failed to fetch Exams", task.getException());
                    }
                    displayEvents();
                });
    }

    private void fetchClubEvents() {
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
                                    .addOnCompleteListener(eventTask -> {
                                        if (eventTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot eventDoc : eventTask.getResult()) {
                                                Map<String, Object> event = new HashMap<>();
                                                event.put("type", "Club Event");
                                                event.put("club", clubName);
                                                event.put("name", eventDoc.getString("event"));
                                                event.put("date", eventDoc.getTimestamp("date"));
                                                eventsList.add(event);
                                            }
                                        } else {
                                            Log.e("FETCH_ERROR", "Failed to fetch Club Events for " + clubName, eventTask.getException());
                                        }
                                        if (clubDoc == task.getResult().getDocuments().get(task.getResult().size() - 1)) {
                                            displayEvents();
                                        }
                                    });
                        }
                    } else {
                        Log.e("FETCH_ERROR", "Failed to fetch Clubs", task.getException());
                        displayEvents();
                    }
                });
        displayEvents();
    }



    private void displayEvents() {

        for (Map<String, Object> event : eventsList) {
            StringBuilder eventDetails = new StringBuilder("Event Details:");
            for (Map.Entry<String, Object> entry : event.entrySet()) {
                eventDetails.append(" ").append(entry.getKey()).append(": ").append(entry.getValue()).append(",");
            }
            Log.d("EVENT_LOG", eventDetails.toString());
        }
    }
}