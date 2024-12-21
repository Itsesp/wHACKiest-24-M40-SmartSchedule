package com.example.smartschedule.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TimetableEntry.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TimetableDao timetableDao();
}

