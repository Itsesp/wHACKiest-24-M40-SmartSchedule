package com.example.smartschedule.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TimetableDao {

    @Insert
    void insert(TimetableEntry timetableEntry);

    @Query("SELECT * FROM timetable WHERE day = :day")
    List<TimetableEntry> getTimetableForDay(String day);

    @Query("SELECT * FROM timetable ORDER BY day, startTime")
    List<TimetableEntry> getAllTimetable();
    @Delete
    void delete(TimetableEntry entry);
    @Query("DELETE FROM timetable")
    void deleteAll();
}

