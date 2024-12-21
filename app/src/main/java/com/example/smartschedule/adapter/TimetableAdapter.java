package com.example.smartschedule.adapter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartschedule.NotificationReceiver;
import com.example.smartschedule.R;
import com.example.smartschedule.database.TimetableEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.TimetableViewHolder> {

    private List<TimetableEntry> timetableEntries;
    private Context context;

    public TimetableAdapter(Context context, List<TimetableEntry> timetableEntries) {
        this.context = context;
        this.timetableEntries = timetableEntries;
    }

    @Override
    public TimetableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timetable_item, parent, false);
        return new TimetableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimetableViewHolder holder, int position) {
        TimetableEntry entry = timetableEntries.get(position);
        holder.dayTextView.setText(entry.getDay());
        holder.subjectTextView.setText(entry.getSubjectName());
        holder.timeTextView.setText(entry.getStartTime() + " - " + entry.getEndTime());

        holder.notifyIcon.setOnClickListener(v -> {
            String startTime = entry.getStartTime();
            createNotificationAtTime();
        });
    }

    @Override
    public int getItemCount() {
        return timetableEntries.size();
    }

    public void updateData(List<TimetableEntry> newEntries) {
        timetableEntries = newEntries;
        notifyDataSetChanged();
    }

    public class TimetableViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView, subjectTextView, timeTextView;
        ImageView notifyIcon;

        public TimetableViewHolder(View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            notifyIcon = itemView.findViewById(R.id.notifyIcon);
        }
    }

    private void createNotificationAtTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!context.getSystemService(AlarmManager.class).canScheduleExactAlarms()) {
                Toast.makeText(context, "Permission to set exact alarms is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String hardcodedTime = "13:30"; // 1:30 PM in 24-hour format

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date hardcodedDate = dateFormat.parse(hardcodedTime);

            if (hardcodedDate != null) {
                // Get the current date to set the alarm for today at the hardcoded time
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(hardcodedDate);

                // Set the alarm time to today at 1:30 PM
                long triggerAtMillis = calendar.getTimeInMillis();

                // If the hardcoded time is already in the past for today, set it for tomorrow
                long currentTime = System.currentTimeMillis();
                Log.d("TimetableAdapter", "Current time in millis: " + currentTime);
                Log.d("TimetableAdapter", "Trigger time in millis (before adjustment): " + triggerAtMillis);

                // If the trigger time is in the past, add 24 hours
                if (triggerAtMillis <= currentTime) {
                    triggerAtMillis += 24 * 60 * 60 * 1000; // Add 24 hours in milliseconds
                    Log.d("TimetableAdapter", "Trigger time adjusted to: " + triggerAtMillis);
                }


                // Create an Intent and PendingIntent for the alarm
                Intent intent = new Intent(context, NotificationReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                // Schedule the alarm
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                }

                Log.d("TimetableAdapter", "Alarm set for: " + hardcodedTime);

            } else {
                Log.e("TimetableAdapter", "Failed to parse hardcoded time.");
            }
        } catch (ParseException e) {
            Log.e("TimetableAdapter", "ParseException: " + e.getMessage());
        }
    }


}
