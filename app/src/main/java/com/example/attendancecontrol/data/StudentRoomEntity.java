package com.example.attendancecontrol.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Строка таблицы students. */
@Entity(
    tableName = "students",
    indices = {
        @Index(value = {"firstName", "lastName", "groupName"}, unique = true)
    }
)
public class StudentRoomEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String firstName;
    public String lastName;
    public String groupName;

    public StudentRoomEntity(int id, String firstName, String lastName, String groupName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.groupName = groupName;
    }
}
