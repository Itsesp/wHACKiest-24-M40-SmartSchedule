package com.example.smartschedule.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartschedule.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpcomingEventListAdapter extends RecyclerView.Adapter<UpcomingEventListAdapter.EventViewHolder> {

    private List<String> sortedEvents;

    public UpcomingEventListAdapter(List<String> sortedEvents) {
        this.sortedEvents = sortedEvents;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        String event = sortedEvents.get(position);
        String[] eventDetails = event.split(" - ");

        // Splitting the date into components
        String eventName = eventDetails[0];
        String eventDate = eventDetails[1];

        try {
            // Parse the event date string to get the day and month
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(eventDate);

            if (date != null) {
                // Extracting day and month
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
                SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
                String day = dayFormat.format(date);
                String month = monthFormat.format(date);

                holder.eventDateTextView.setText(day);  // Large date
                holder.eventMonthTextView.setText(month);  // Short month name
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.eventNameTextView.setText(eventName);  // Event Name
    }

    @Override
    public int getItemCount() {
        return sortedEvents.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView eventDateTextView;
        TextView eventMonthTextView;
        TextView eventNameTextView;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventDateTextView = itemView.findViewById(R.id.eventDateTextView);
            eventMonthTextView = itemView.findViewById(R.id.eventMonthTextView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        }
    }
}
