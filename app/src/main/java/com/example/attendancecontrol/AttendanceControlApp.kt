package com.example.attendancecontrol

import android.app.Application
import com.example.attendancecontrol.data.AppDatabase

/**
 * Точка инициализации локальной базы Room.
 * База создаётся один раз и используется репозиториями приложения.
 */
class AttendanceControlApp : Application() {

    companion object {
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
    }
}
