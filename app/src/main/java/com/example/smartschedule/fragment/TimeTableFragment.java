package com.example.smartschedule.fragment;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartschedule.R;
import com.example.smartschedule.adapter.TimetableAdapter;
import com.example.smartschedule.database.AppDatabase;
import com.example.smartschedule.database.AppDatabaseProvider;
import com.example.smartschedule.database.TimetableEntry;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TimeTableFragment extends Fragment {

    private static final String TAG = "TimetableFragment";
    private AppDatabase database;
    private RecyclerView recyclerView;
    private TimetableAdapter timetableAdapter;
    private List<TimetableEntry> timetableEntries = new ArrayList<>();
    private List<String> days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday","Saturday");


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_time_table, container, false);
        database = AppDatabaseProvider.getDatabase(getContext());
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        timetableAdapter = new TimetableAdapter(requireContext(),timetableEntries);
        recyclerView.setAdapter(timetableAdapter);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        if (toolbar != null) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.background));
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimary));
            toolbar.setTitle("Time Table");
        }
        setupDayButtons(rootView);
        fetchTimetableData();

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupDayButtons(View view) {
        LinearLayout daysButtonLayout = view.findViewById(R.id.daysButtonLayout);

        for (String day : days) {
            Button dayButton = new Button(getContext());
            dayButton.setText(day);
            dayButton.setTextColor(getResources().getColor(R.color.white));
            dayButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            dayButton.setBackgroundResource(R.drawable.button_background);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f); // 0 width, weight=1 to equally distribute space
            layoutParams.setMargins(16, 8, 16, 8);  // Add left, top, right, and bottom margins

            dayButton.setLayoutParams(layoutParams);
            dayButton.setPadding(16, 8, 20, 8);
            dayButton.setOnClickListener(v -> filterTimetableByDay(day));

            daysButtonLayout.addView(dayButton);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void fetchTimetableData() {
        new Thread(() -> {
            // Fetch all timetable entries from the database
            List<TimetableEntry> entries = database.timetableDao().getAllTimetable();

            if (entries != null && !entries.isEmpty()) {
                timetableEntries = entries;
                getActivity().runOnUiThread(() -> {
                    timetableAdapter.updateData(entries);
                    LocalDate currentDate = LocalDate.now();
                    String today = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());
                    if(today.equals("Sunday"))today="Monday";
                    Log.d(TAG, "Today's day: " + today);
                    filterTimetableByDay(today);
                });
            } else {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "No timetable entries found.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void filterTimetableByDay(String selectedDay) {

        List<TimetableEntry> filteredList = new ArrayList<>();
        for (TimetableEntry entry : timetableEntries) {
            if (entry.getDay().equals(selectedDay)) {
                filteredList.add(entry);
            }
        }

        Collections.sort(filteredList, (entry1, entry2) -> {

            String startTime1 = normalizeTime(entry1.getStartTime());
            String startTime2 = normalizeTime(entry2.getStartTime());

            LocalTime time1 = LocalTime.parse(startTime1);
            LocalTime time2 = LocalTime.parse(startTime2);

            return time1.compareTo(time2);
        });

        timetableAdapter.updateData(filteredList);
    }

    private String normalizeTime(String time) {
        String[] timeParts = time.split(":");
        if (timeParts[0].length() == 1) {
            return "0" + time;
        }
        return time;
    }

}
