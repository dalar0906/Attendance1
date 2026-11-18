package com.example.attendancecontrol.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Справочник предметов; normalizedName обеспечивает отсутствие дублей без учёта регистра. */
@Entity(tableName = "subjects")
public class SubjectRoomEntity {
    @PrimaryKey
    @NonNull
    public String normalizedName;

    @NonNull
    public String displayName;

    public SubjectRoomEntity(@NonNull String normalizedName, @NonNull String displayName) {
        this.normalizedName = normalizedName;
        this.displayName = displayName;
    }
}
