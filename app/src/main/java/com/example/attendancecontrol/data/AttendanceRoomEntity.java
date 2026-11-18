package com.example.attendancecontrol.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * Одна отметка: студент + дата + предмет.
 * Составной первичный ключ не позволяет создать две одинаковые отметки.
 */
@Entity(
    tableName = "attendance",
    primaryKeys = {"studentId", "lessonDate", "subject"},
    foreignKeys = {
        @ForeignKey(
            entity = StudentRoomEntity.class,
            parentColumns = {"id"},
            childColumns = {"studentId"},
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("studentId"),
        @Index(value = {"lessonDate", "subject"})
    }
)
public class AttendanceRoomEntity {
    public int studentId;

    @NonNull
    public String lessonDate;

    @NonNull
    public String subject;

    @NonNull
    public String status;

    public AttendanceRoomEntity(int studentId, @NonNull String lessonDate,
                                 @NonNull String subject, @NonNull String status) {
        this.studentId = studentId;
        this.lessonDate = lessonDate;
        this.subject = subject;
        this.status = status;
    }
}
