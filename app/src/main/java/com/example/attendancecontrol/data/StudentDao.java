package com.example.attendancecontrol.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StudentDao {
    @Query("SELECT * FROM students ORDER BY lastName COLLATE NOCASE, firstName COLLATE NOCASE")
    List<StudentRoomEntity> getAll();

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    StudentRoomEntity getById(int id);

    @Query("SELECT * FROM students WHERE groupName = :groupName COLLATE NOCASE " +
           "ORDER BY lastName COLLATE NOCASE, firstName COLLATE NOCASE")
    List<StudentRoomEntity> getByGroup(String groupName);

    @Query("SELECT DISTINCT groupName FROM students ORDER BY groupName COLLATE NOCASE")
    List<String> getGroups();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(StudentRoomEntity student);

    @Query("DELETE FROM students WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT EXISTS(SELECT 1 FROM students " +
           "WHERE firstName = :firstName COLLATE NOCASE " +
           "AND lastName = :lastName COLLATE NOCASE " +
           "AND groupName = :groupName COLLATE NOCASE)")
    boolean exists(String firstName, String lastName, String groupName);
}
