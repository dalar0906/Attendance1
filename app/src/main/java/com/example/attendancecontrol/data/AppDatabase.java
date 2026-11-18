package com.example.attendancecontrol.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/** Единая база данных приложения. */
@Database(
    entities = {
        StudentRoomEntity.class,
        AttendanceRoomEntity.class,
        SubjectRoomEntity.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract StudentDao studentDao();
    public abstract AttendanceDao attendanceDao();
    public abstract SubjectDao subjectDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "attendance_control.db"
                    )
                    // Оставлено для совместимости с синхронными методами текущих экранов.
                    // Для крупного приложения лучше перевести обращения на корутины/фоновые потоки.
                    .allowMainThreadQueries()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
