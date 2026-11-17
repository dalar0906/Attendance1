package com.example.attendancecontrol.ui

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.AttendanceRepository
import com.example.attendancecontrol.data.StudentRepository
import com.example.attendancecontrol.data.SubjectRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StudentAttendanceActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_SAVED_KEYS = "extra_saved_keys"
    }

    private lateinit var tvStudentInfo: TextView
    private lateinit var btnDate: Button
    private lateinit var etNewSubject: EditText
    private lateinit var btnAddSubject: Button
    private lateinit var lvSubjects: ListView
    private lateinit var tvEmpty: TextView
    private lateinit var btnSave: Button

    private var studentId: Int = -1
    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    private val subjects = mutableListOf<String>()
    private val statuses = mutableMapOf<String, String?>()
    private lateinit var adapter: SubjectStatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attendance)

        studentId = intent.getIntExtra(EXTRA_STUDENT_ID, -1)
        val initialDate = intent.getStringExtra(EXTRA_DATE)

        if (!initialDate.isNullOrBlank()) {
            try {
                dateFormat.parse(initialDate)?.let { calendar.time = it }
            } catch (_: Exception) { }
        }

        tvStudentInfo = findViewById(R.id.tvStudentInfo)
        btnDate = findViewById(R.id.btnDate)
        etNewSubject = findViewById(R.id.etNewSubject)
        btnAddSubject = findViewById(R.id.btnAddSubject)
        lvSubjects = findViewById(R.id.lvSubjects)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnSave = findViewById(R.id.btnSave)

        val student = StudentRepository.getById(studentId)
        if (student == null) {
            Toast.makeText(this, "Студент не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        tvStudentInfo.text = "${student.lastName} ${student.firstName} • ${student.groupName}"

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }

        updateDateButton()
        btnDate.setOnClickListener { showDatePicker() }
        btnAddSubject.setOnClickListener { addSubject() }
        btnSave.setOnClickListener { save() }

        // Загружаем список предметов
        subjects.clear()
        subjects.addAll(SubjectRepository.getAll())
        subjects.forEach { statuses[it] = null }

        adapter = SubjectStatusAdapter(this, subjects, statuses) { }
        lvSubjects.adapter = adapter

        loadExistingStatuses()
        refreshEmpty()
    }

    private fun updateDateButton() {
        btnDate.text = "Дата: ${dateFormat.format(calendar.time)}"
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                updateDateButton()
                loadExistingStatuses()
                adapter.notifyDataSetChanged()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addSubject() {
        val raw = etNewSubject.text.toString()
        if (!SubjectRepository.isValid(raw)) {
            Toast.makeText(this, "Введите предмет (2–50 символов)", Toast.LENGTH_SHORT).show()
            return
        }
        val subject = SubjectRepository.add(raw)

        if (subjects.any { it.equals(subject, ignoreCase = true) }) {
            Toast.makeText(this, "Этот предмет уже в списке", Toast.LENGTH_SHORT).show()
            return
        }

        subjects.add(subject)
        statuses[subject] = null
        etNewSubject.text.clear()
        adapter.notifyDataSetChanged()
        refreshEmpty()
    }

    private fun loadExistingStatuses() {
        val date = dateFormat.format(calendar.time)
        val saved = AttendanceRepository.getStudentStatuses(studentId, date)
        subjects.forEach { subject ->
            statuses[subject] = saved.entries
                .find { it.key.equals(subject, ignoreCase = true) }
                ?.value
        }
    }

    private fun refreshEmpty() {
        val empty = subjects.isEmpty()
        tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        lvSubjects.visibility = if (empty) View.GONE else View.VISIBLE
        btnSave.isEnabled = !empty
    }

    private fun save() {
        val date = dateFormat.format(calendar.time)

        // Только предметы, у которых выбран статус
        val marks = statuses
            .filter { it.value != null }
            .mapValues { it.value!! }

        if (marks.isEmpty()) {
            Toast.makeText(this, "Выберите хотя бы один статус", Toast.LENGTH_SHORT).show()
            return
        }

        // Сохраняем сразу несколько предметов
        AttendanceRepository.saveStudentMarks(studentId, date, marks)

        val savedKeys = ArrayList(marks.keys.map { "$date|$it" })

        Toast.makeText(this, "Сохранено предметов: ${marks.size}", Toast.LENGTH_SHORT).show()

        setResult(
            RESULT_OK,
            Intent().putStringArrayListExtra(EXTRA_SAVED_KEYS, savedKeys)
        )
        finish()
    }
}