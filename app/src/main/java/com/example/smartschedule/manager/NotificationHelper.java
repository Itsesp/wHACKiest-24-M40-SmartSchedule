package com.example.smartschedule.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.smartschedule.NotificationReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    public static void scheduleNotification(Context context, String title, String message, long eventTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);

        intent.putExtra("title", title);
        intent.putExtra("message", message);

        int pendingIntentFlags = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ?
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, pendingIntentFlags);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(new Date(eventTime));  // Format the event time
        Log.d("Clubs", "Notification scheduled for: " + formattedDate); // Log the scheduled time


        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, eventTime, pendingIntent);
            Toast.makeText(context, "Notification Scheduled", Toast.LENGTH_SHORT).show();
        }
    }
}
