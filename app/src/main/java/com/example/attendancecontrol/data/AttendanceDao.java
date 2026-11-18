package com.example.attendancecontrol.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY lessonDate DESC, subject COLLATE NOCASE")
    List<AttendanceRoomEntity> getAll();

    @Query("SELECT * FROM attendance WHERE lessonDate = :date " +
           "AND subject = :subject COLLATE NOCASE")
    List<AttendanceRoomEntity> getForLesson(String date, String subject);

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND lessonDate = :date")
    List<AttendanceRoomEntity> getForStudentAndDate(int studentId, String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AttendanceRoomEntity> records);

    @Query("DELETE FROM attendance WHERE studentId = :studentId")
    void deleteForStudent(int studentId);

    @Query("DELETE FROM attendance WHERE lessonDate = :date AND subject = :subject COLLATE NOCASE " +
           "AND studentId IN (:studentIds)")
    void deleteForLessonStudents(String date, String subject, List<Integer> studentIds);

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND lessonDate = :date")
    void deleteForStudentAndDate(int studentId, String date);

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND lessonDate = :date " +
           "AND subject = :subject COLLATE NOCASE")
    void deleteForStudentDateSubject(int studentId, String date, String subject);
}
