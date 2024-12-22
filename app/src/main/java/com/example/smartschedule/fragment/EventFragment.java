package com.example.smartschedule.fragment;


import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.smartschedule.R;
import com.example.smartschedule.adapter.DaysAdapter;
import com.example.smartschedule.adapter.EventListAdapter;
import com.example.smartschedule.data.DayData;
import com.example.smartschedule.manager.ClubManager;
import com.example.smartschedule.manager.ExamManager;
import com.example.smartschedule.manager.HolidayManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class EventFragment extends Fragment {

    private RecyclerView recyclerView;
    private DaysAdapter daysAdapter;
    private List<DayData> dayList;
    private HolidayManager holidayManager;
    private ExamManager examManager;
    private ClubManager clubManager;
    private List<String> currentEvents;
    private EventListAdapter eventListAdapter;
    private RecyclerView eventsRecyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewDays);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        eventsRecyclerView = view.findViewById(R.id.recyclerViewEvents);
        dayList = new ArrayList<>();
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        currentEvents = new ArrayList<>();
        holidayManager = new HolidayManager();
        clubManager = new ClubManager();
        examManager=new ExamManager();
        if (toolbar != null) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
            toolbar.setTitle("Events");
        }
        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        List<HolidayManager.Holiday> holidays = holidayManager.loadHolidaysLocally(getContext());
        List<ExamManager.Exam> examDays = examManager.loadExamsLocally(getContext());
        List<ClubManager.ClubEvent> clubEvents = clubManager.loadClubEventsLocally(getContext());
        eventListAdapter = new EventListAdapter(currentEvents);
        eventsRecyclerView.setAdapter(eventListAdapter);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        for (int i = 1; i <= daysInMonth; i++) {
            calendar.set(Calendar.DAY_OF_MONTH, i);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            boolean isHoliday = false, isExamDay = false, isEventDay = false, isOtherEventDay = false;
            String currentDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", currentYear, currentMonth + 1, i);

            String dayName = getDayName(dayOfWeek);
            List<String> eventsForTheDay = new ArrayList<>(); // Collect events for the day

            for (ClubManager.ClubEvent event : clubEvents) {
                if (event.getDate().equals(currentDate)) {
                    isEventDay = true;
                    eventsForTheDay.add(event.getEventName()); // Add club event name
                }
            }

            for (HolidayManager.Holiday event : holidays) {
                if (event.getDate().equals(currentDate)) {
                    isHoliday = true;
                    eventsForTheDay.add(event.getName()); // Add holiday name
                }
            }

            for (ExamManager.Exam event : examDays) {
                if (event.getDate().equals(currentDate)) {
                    isExamDay = true;
                    eventsForTheDay.add(event.getExamName()); // Add exam name
                }
            }

            dayList.add(new DayData(dayName, i, isHoliday, isExamDay, isEventDay, isOtherEventDay, eventsForTheDay));
        }

        // Set the adapter
        daysAdapter = new DaysAdapter(dayList,this::onDayClicked);
        recyclerView.setAdapter(daysAdapter);
        recyclerView.post(() -> {
            if (daysAdapter != null && daysAdapter.selectedPosition != -1) {
                recyclerView.scrollToPosition(daysAdapter.selectedPosition);
            }
        });
        return view;
    }
    private void onDayClicked(DayData dayData) {
        currentEvents.clear();
        if (dayData.getEvents() != null) {
            currentEvents.addAll(dayData.getEvents());
        }
        eventListAdapter.notifyDataSetChanged();
        Log.d("EventFrag","Clicked"+currentEvents.toString());
    }
    private String getDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "SUN";
            case Calendar.MONDAY: return "MON";
            case Calendar.TUESDAY: return "TUE";
            case Calendar.WEDNESDAY: return "WED";
            case Calendar.THURSDAY: return "THU";
            case Calendar.FRIDAY: return "FRI";
            case Calendar.SATURDAY: return "SAT";
            default: return "";
        }
    }
}