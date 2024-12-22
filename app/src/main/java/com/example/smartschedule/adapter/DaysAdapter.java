package com.example.smartschedule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartschedule.R;
import com.example.smartschedule.data.DayData;

import java.util.List;
import java.util.Calendar;

public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DayViewHolder> {
    private List<DayData> dayList;
    public int selectedPosition = -1; // Initially, no day is selected.
    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(DayData dayData);
    }

    public DaysAdapter(List<DayData> dayList, OnDayClickListener onDayClickListener) {
        this.dayList = dayList;
        this.onDayClickListener = onDayClickListener;
        setTodaysDateAsSelected(); // Set today's date as selected.
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.day_item, parent, false); // Use the layout you provided (item_day.xml)
        return new DayViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayData dayData = dayList.get(position);

        holder.dayView.setText(dayData.getDayName());
        holder.dayNumber.setText(String.valueOf(dayData.getDayOfMonth()));

        holder.holidayColor.setVisibility(dayData.isHoliday() ? View.VISIBLE : View.GONE);
        holder.examColor.setVisibility(dayData.isExamDay() ? View.VISIBLE : View.GONE);
        holder.eventColor.setVisibility(dayData.isEventDay() ? View.VISIBLE : View.GONE);
        holder.otherColor.setVisibility(dayData.isOtherEventDay() ? View.VISIBLE : View.GONE);

        // Update the background color based on selection
        if (holder.getAdapterPosition() == selectedPosition) {

            holder.dayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primaryText));
            holder.dayNumber.setBackgroundResource(R.color.white);
            holder.linearLayout.setBackgroundResource(R.drawable.selected_day_box_background);
        } else {

            holder.dayNumber.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.dayNumber.setBackgroundResource(R.color.colorPrimary);
            holder.linearLayout.setBackgroundResource(R.color.colorPrimary);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onDayClickListener != null) {
                onDayClickListener.onDayClick(dayList.get(holder.getAdapterPosition()));
            }
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                selectedPosition = currentPosition; // Update selected day
                notifyDataSetChanged(); // Refresh the RecyclerView to update the background
            }

        });

    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    private void setTodaysDateAsSelected() {
        Calendar calendar = Calendar.getInstance();
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < dayList.size(); i++) {
            if (dayList.get(i).getDayOfMonth() == todayDay) {
                selectedPosition = i;
                break;
            }
        }
    }

    public  class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayView;
        TextView dayNumber;
        LinearLayout linearLayout;
        View holidayColor, examColor, eventColor, otherColor;

        public DayViewHolder(View itemView) {
            super(itemView);
            linearLayout=itemView.findViewById(R.id.selectDate);
            dayView = itemView.findViewById(R.id.dayView);
            dayNumber = itemView.findViewById(R.id.dayNumber);
            holidayColor = itemView.findViewById(R.id.holidayColor);
            examColor = itemView.findViewById(R.id.ExamColor);
            eventColor = itemView.findViewById(R.id.EventColor);
            otherColor = itemView.findViewById(R.id.OtherColor);
            itemView.setOnClickListener(v -> {
                if (onDayClickListener != null) {
                    onDayClickListener.onDayClick(dayList.get(getAdapterPosition()));
                }
            });
        }
    }
}
