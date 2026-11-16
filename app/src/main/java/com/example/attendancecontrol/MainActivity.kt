package com.example.attendancecontrol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.attendancecontrol.ui.AttendanceActivity
import com.example.attendancecontrol.ui.StatisticsActivity
import com.example.attendancecontrol.ui.StudentsActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Кнопка «Студенты»
        findViewById<Button>(R.id.btnStudents).setOnClickListener {
            startActivity(Intent(this, StudentsActivity::class.java))
        }

        // Кнопка «Отметить посещаемость»
        findViewById<Button>(R.id.btnAttendance).setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
        }

        // Кнопка «Статистика»
        findViewById<Button>(R.id.btnStatistics).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }
    }
}