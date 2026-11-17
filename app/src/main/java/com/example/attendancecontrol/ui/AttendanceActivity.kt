package com.example.attendancecontrol.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.StudentRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Экран занятия: выбор группы, даты и предмета.
 * Дальше переход на MarkAttendanceActivity со списком студентов.
 */
class AttendanceActivity : AppCompatActivity() {

    private lateinit var spGroup: Spinner
    private lateinit var btnDate: Button
    private lateinit var etSubject: EditText
    private lateinit var btnOpenList: Button
    private lateinit var tvNoStudents: TextView

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
    private var groups: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        spGroup = findViewById(R.id.spGroup)
        btnDate = findViewById(R.id.btnDate)
        etSubject = findViewById(R.id.etSubject)
        btnOpenList = findViewById(R.id.btnOpenList)
        tvNoStudents = findViewById(R.id.tvNoStudents)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        updateDateButton()
        btnDate.setOnClickListener { showDatePicker() }
        btnOpenList.setOnClickListener { openList() }
    }

    override fun onResume() {
        super.onResume()
        refreshGroups()
    }

    private fun refreshGroups() {
        val selected = spGroup.selectedItem as? String
        groups = StudentRepository.getGroups()

        spGroup.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            groups
        )
        selected?.let {
            val index = groups.indexOf(it)
            if (index >= 0) spGroup.setSelection(index)
        }

        val hasGroups = groups.isNotEmpty()
        tvNoStudents.visibility = if (hasGroups) View.GONE else View.VISIBLE
        spGroup.isEnabled = hasGroups
        btnOpenList.isEnabled = hasGroups
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                updateDateButton()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateButton() {
        btnDate.text = dateFormat.format(calendar.time)
    }

    private fun openList() {
        val group = spGroup.selectedItem as? String
        if (group == null) {
            Toast.makeText(this, "Сначала добавьте студентов", Toast.LENGTH_SHORT).show()
            return
        }

        val subject = etSubject.text.toString().trim()
        if (subject.length < 2) {
            Toast.makeText(this, "Введите предмет (минимум 2 символа)", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, MarkAttendanceActivity::class.java).apply {
            putExtra(MarkAttendanceActivity.EXTRA_GROUP, group)
            putExtra(MarkAttendanceActivity.EXTRA_DATE, dateFormat.format(calendar.time))
            putExtra(MarkAttendanceActivity.EXTRA_SUBJECT, subject)
        }
        startActivity(intent)
    }
}
