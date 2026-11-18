package com.example.attendancecontrol.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SubjectDao {
    @Query("SELECT displayName FROM subjects ORDER BY displayName COLLATE NOCASE")
    List<String> getAll();

    @Query("SELECT displayName FROM subjects WHERE normalizedName = :normalizedName LIMIT 1")
    String getByNormalizedName(String normalizedName);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(SubjectRoomEntity subject);
}
