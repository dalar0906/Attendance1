package com.example.attendancecontrol.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.AttendanceRepository
import com.example.attendancecontrol.data.AttendanceStatus
import com.example.attendancecontrol.data.StudentRepository

/**
 * Экран отметки: список студентов выбранной группы, для каждого — статус.
 * Статус можно менять сколько угодно до нажатия «Сохранить».
 */
class MarkAttendanceActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP = "extra_group"
        const val EXTRA_DATE = "extra_date"
        const val EXTRA_SUBJECT = "extra_subject"
    }

    private lateinit var tvSummary: TextView
    private lateinit var btnSave: Button

    private lateinit var group: String
    private lateinit var date: String
    private lateinit var subject: String

    private lateinit var adapter: AttendanceAdapter
    private val statuses = mutableMapOf<Int, String>()
    // Состояние на момент последнего сохранения/открытия — нужно, чтобы знать об изменениях
    private var savedSnapshot: Map<Int, String> = emptyMap()

    // Экран студента сообщает, какие «дата|предмет» он сохранил
    private val studentScreen =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val keys = result.data?.getStringArrayListExtra(StudentAttendanceActivity.EXTRA_SAVED_KEYS)
            if (keys != null) applySavedFromStudentScreen(keys)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mark_attendance)

        group = intent.getStringExtra(EXTRA_GROUP).orEmpty()
        date = intent.getStringExtra(EXTRA_DATE).orEmpty()
        subject = intent.getStringExtra(EXTRA_SUBJECT).orEmpty()

        val tvLessonInfo = findViewById<TextView>(R.id.tvLessonInfo)
        tvSummary = findViewById(R.id.tvSummary)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val lvAttendance = findViewById<ListView>(R.id.lvAttendance)
        btnSave = findViewById(R.id.btnSave)

        tvLessonInfo.text = "$group • $date • $subject"

        val students = StudentRepository.getByGroup(group)

        // Пустой список → понятное сообщение
        if (students.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            lvAttendance.visibility = View.GONE
            btnSave.isEnabled = false
            tvSummary.visibility = View.GONE
        } else {
            // По умолчанию «Присутствовал»; если занятие уже отмечали — подставляем сохранённое
            val previous = AttendanceRepository.getStatuses(date, subject)
            students.forEach { statuses[it.id] = previous[it.id] ?: AttendanceStatus.PRESENT }
            savedSnapshot = statuses.toMap()

            adapter = AttendanceAdapter(this, students, statuses) { updateSummary() }
            lvAttendance.adapter = adapter
            updateSummary()

            // Нажатие на студента → экран студента (несколько предметов сразу)
            lvAttendance.setOnItemClickListener { _, _, position, _ ->
                val student = students[position]
                studentScreen.launch(
                    Intent(this, StudentAttendanceActivity::class.java).apply {
                        putExtra(StudentAttendanceActivity.EXTRA_STUDENT_ID, student.id)
                        putExtra(StudentAttendanceActivity.EXTRA_DATE, date)
                    }
                )
            }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener { handleBack() }
        btnSave.setOnClickListener { save() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = handleBack()
        })
    }

    private fun updateSummary() {
        val present = statuses.values.count { it == AttendanceStatus.PRESENT }
        val absent = statuses.values.count { it == AttendanceStatus.ABSENT }
        val late = statuses.values.count { it == AttendanceStatus.LATE }
        tvSummary.text = "Присутствуют: $present • Отсутствуют: $absent • Опоздали: $late"
    }

    /**
     * Если на экране студента сохранили отметку по текущему занятию (дата + предмет),
     * подтягиваем её в список. Остальные несохранённые правки не затрагиваются.
     */
    private fun applySavedFromStudentScreen(keys: List<String>) {
        val touched = keys.any { key ->
            val parts = key.split("|", limit = 2)
            parts.size == 2 && parts[0] == date && parts[1].equals(subject, ignoreCase = true)
        }
        if (!touched || !::adapter.isInitialized) return

        val saved = AttendanceRepository.getStatuses(date, subject)
        val snapshot = savedSnapshot.toMutableMap()
        for (i in 0 until adapter.count) {
            val id = adapter.getItem(i).id
            val status = saved[id] ?: continue
            // Не трогаем студентов, чьи правки на этом экране ещё не сохранены
            if (statuses[id] == savedSnapshot[id] || statuses[id] == status) {
                statuses[id] = status
                snapshot[id] = status
            }
        }
        savedSnapshot = snapshot
        adapter.notifyDataSetChanged()
        updateSummary()
    }

    private fun save() {
        AttendanceRepository.saveLesson(date, subject, statuses)
        savedSnapshot = statuses.toMap()
        Toast.makeText(this, "Посещаемость сохранена", Toast.LENGTH_SHORT).show()
    }

    private fun hasUnsavedChanges(): Boolean = statuses != savedSnapshot

    private fun handleBack() {
        if (!hasUnsavedChanges()) {
            finish()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Несохранённые изменения")
            .setMessage("Отметки не сохранены. Выйти без сохранения?")
            .setPositiveButton("Выйти") { _, _ -> finish() }
            .setNegativeButton("Остаться", null)
            .show()
    }
}