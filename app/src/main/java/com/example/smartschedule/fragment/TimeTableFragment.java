package com.example.smartschedule.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.smartschedule.R;
import com.example.smartschedule.database.AppDatabase;
import com.example.smartschedule.database.AppDatabaseProvider;
import com.example.smartschedule.database.TimetableEntry;
import android.util.Log;
import java.util.List;

public class TimeTableFragment extends Fragment {

    private static final String TAG = "TimetableFragment";
    private AppDatabase database;

    public TimeTableFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_time_table, container, false);
        database = AppDatabaseProvider.getDatabase(getContext());

        loadAndLogTimetable();

        return rootView;
    }

    private void loadAndLogTimetable() {
        // Use a background thread to fetch timetable data from Room
        new Thread(() -> {
            List<TimetableEntry> timetableEntries = database.timetableDao().getAllTimetable();

            if (timetableEntries != null && !timetableEntries.isEmpty()) {
                for (TimetableEntry entry : timetableEntries) {
                    Log.d(TAG, "Day: " + entry.getDay() +
                            ", Subject: " + entry.getSubjectName() +
                            ", Start Time: " + entry.getStartTime() +
                            ", End Time: " + entry.getEndTime());
                }
            } else {
                Log.d(TAG, "No timetable entries found.");
            }
        }).start();
    }
}
