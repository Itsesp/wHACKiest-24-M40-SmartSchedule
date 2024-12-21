package com.example.smartschedule.database;

import android.content.Context;

import androidx.room.Room;

public class AppDatabaseProvider {
    private static AppDatabase appDatabase;

    public static AppDatabase getDatabase(Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "timetable_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return appDatabase;
    }
}

