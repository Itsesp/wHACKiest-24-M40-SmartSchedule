package com.example.smartschedule.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.example.smartschedule.manager.ClubManager;
import com.example.smartschedule.manager.ExamManager;
import com.example.smartschedule.manager.HolidayManager;
import com.example.smartschedule.R;
import com.example.smartschedule.adapter.AttendanceAdapter;
import com.example.smartschedule.adapter.EventListAdapter;
import com.example.smartschedule.adapter.UpcomingEventListAdapter;
import com.example.smartschedule.data.AttendanceItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
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
    private RecyclerView recyclerView , attendanceRecycler;
    private AttendanceAdapter adapter;
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
        recyclerView=view.findViewById(R.id.upcomingView);
        attendanceRecycler=view.findViewById(R.id.attendanceView);
        int numberOfColumns = 3;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), numberOfColumns);
        attendanceRecycler.setLayoutManager(gridLayoutManager);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        //holidayManager.fetchAndUpdateHolidays(requireContext());
        //examManager.fetchAndUpdateExams(requireContext());
        //clubManager.fetchAndUpdateClubEvents(requireContext());
        checkAndFetchData();
        setEventColors();
        loadAndSortEvents();
        mKalendarView.setInitialSelectedDate(new Date());
        mKalendarView.setDateSelector(new KalendarView.DateSelector() {
            @Override
            public void onDateClicked(Date selectedDate) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                List<String> associatedEvents = new ArrayList<>();

                // Check for holidays
                for (HolidayManager.Holiday holiday : holidayManager.loadHolidaysLocally(requireContext())) {
                    try {
                        Date holidayDate = sdf.parse(holiday.getDate());
                        if (holidayDate != null && isSameDay(selectedDate, holidayDate)) {
                            associatedEvents.add(holiday.getName());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Check for exams
                for (ExamManager.Exam exam : examManager.loadExamsLocally(requireContext())) {
                    try {
                        Date examDate = sdf.parse(exam.getDate());
                        if (examDate != null && isSameDay(selectedDate, examDate)) {
                            associatedEvents.add(exam.getExamName());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Check for club events
                for (ClubManager.ClubEvent clubEvent : clubManager.loadClubEventsLocally(requireContext())) {
                    try {
                        Date clubEventDate = sdf.parse(clubEvent.getDate());
                        if (clubEventDate != null && isSameDay(selectedDate, clubEventDate)) {
                            associatedEvents.add(clubEvent.getEventName());
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (associatedEvents.size() > 0) {
                    showEventListBottomSheet(associatedEvents);
                    Log.d("Selected Date Events", "Events on " + selectedDate.toString() + ": " + String.join(", ", associatedEvents));
                } else {
                    Log.d("Selected Date Events", "No events found on " + selectedDate.toString());
                }
            }

            private boolean isSameDay(Date date1, Date date2) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                return sdf.format(date1).equals(sdf.format(date2));
            }
            private void showEventListBottomSheet(List<String> associatedNames) {
                View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_event_list, null);

                RecyclerView recyclerView = bottomSheetView.findViewById(R.id.rv_event_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                EventListAdapter adapter = new EventListAdapter(associatedNames);
                recyclerView.setAdapter(adapter);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }

        });
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String usn = preferences.getString("usn", null);

        if (usn != null) {
            fetchAttendanceData(usn);
        } else {
            Toast.makeText(getContext(), "USN not found!", Toast.LENGTH_SHORT).show();
        }
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
    private void loadAndSortEvents() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<String> todayEvents = new ArrayList<>();
        List<String> thisMonthEvents = new ArrayList<>();

        List<String> sortedEvents = new ArrayList<>();

        List<HolidayManager.Holiday> holidays = holidayManager.loadHolidaysLocally(requireContext());
        List<ExamManager.Exam> exams = examManager.loadExamsLocally(requireContext());
        List<ClubManager.ClubEvent> clubEvents = clubManager.loadClubEventsLocally(requireContext());

        for (HolidayManager.Holiday holiday : holidays) {
            try {
                Date holidayDate = sdf.parse(holiday.getDate());
                if (holidayDate != null) {
                    String eventString = holiday.getName() + " - " + sdf.format(holidayDate);
                    if (isSameDay(holidayDate, new Date())) {
                        todayEvents.add(eventString);
                    } else if (isInCurrentMonth(holidayDate)) {
                        thisMonthEvents.add(eventString);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (ExamManager.Exam exam : exams) {
            try {
                Date examDate = sdf.parse(exam.getDate());
                if (examDate != null) {
                    String eventString = exam.getExamName() + " - " + sdf.format(examDate);
                    if (isSameDay(examDate, new Date())) {
                        todayEvents.add(eventString);
                    } else if (isInCurrentMonth(examDate)) {
                        thisMonthEvents.add(eventString);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (ClubManager.ClubEvent clubEvent : clubEvents) {
            try {
                Date clubEventDate = sdf.parse(clubEvent.getDate());
                if (clubEventDate != null) {
                    String eventString = clubEvent.getEventName() + " - " + sdf.format(clubEventDate);
                    if (isSameDay(clubEventDate, new Date())) {
                        todayEvents.add(eventString);
                    } else if (isInCurrentMonth(clubEventDate)) {
                        thisMonthEvents.add(eventString);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Log.d("UpcomingEvents", "Today Events: " + todayEvents.toString());
        Log.d("UpcomingEvents", "This Month Events: " + thisMonthEvents.toString());

        sortedEvents.addAll(todayEvents);
        sortedEvents.addAll(thisMonthEvents);
        Log.d("UpcomingEvents", "Sorted Events: " + sortedEvents.toString());
        UpcomingEventListAdapter adapter = new UpcomingEventListAdapter(sortedEvents);
        recyclerView.setAdapter(adapter);

    }


    private boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date1).equals(sdf.format(date2));
    }

    private boolean isInCurrentMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        Calendar eventDate = Calendar.getInstance();
        eventDate.setTime(date);
        return eventDate.get(Calendar.MONTH) == currentMonth && eventDate.get(Calendar.YEAR) == currentYear;
    }
    private void fetchAttendanceData(String usn) {
        db.collection("Attendance").document(usn).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<AttendanceItem> attendanceList = new ArrayList<>();
                        Map<String, Object> data = documentSnapshot.getData();

                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            String subject = entry.getKey();
                            int percentage = Integer.parseInt(entry.getValue().toString());
                            attendanceList.add(new AttendanceItem(subject, percentage));
                        }
                        attendanceList.sort(Comparator.comparingInt(AttendanceItem::getPercentage));

                        adapter = new AttendanceAdapter(attendanceList);
                        attendanceRecycler.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "No attendance data found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void checkAndFetchData() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);

        long lastFetchTimestamp = preferences.getLong("lastFetchTimestamp", 0);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("timestamp")
                .document("timestamp")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        Timestamp firestoreTimestamp = documentSnapshot.getTimestamp("timestamp");
                        if (firestoreTimestamp != null) {
                            long timestampInMillis = firestoreTimestamp.getSeconds() * 1000;  // Convert to milliseconds
                            if (timestampInMillis > lastFetchTimestamp) {
                                holidayManager.fetchAndUpdateHolidays(requireContext());
                                examManager.fetchAndUpdateExams(requireContext());
                                clubManager.fetchAndUpdateClubEvents(requireContext());
                                updateLastFetchTimestamp(timestampInMillis);
                            } else {

                                Log.d("HomeFragment", "Data is up to date.");
                            }
                        }

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HomeFragment", "Error checking timestamp", e);

                });
    }
    private void updateLastFetchTimestamp(long timestamp) {
        SharedPreferences preferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("lastFetchTimestamp", timestamp); // Store the latest timestamp
        editor.apply();
    }


}