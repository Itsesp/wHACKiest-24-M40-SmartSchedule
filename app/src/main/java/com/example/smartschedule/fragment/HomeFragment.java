package com.example.smartschedule.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ak.ColoredDate;
import com.ak.EventObjects;
import com.ak.KalendarView;
import com.example.smartschedule.HolidayManager;
import com.example.smartschedule.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private KalendarView mKalendarView;
    private HolidayManager holidayManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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
}