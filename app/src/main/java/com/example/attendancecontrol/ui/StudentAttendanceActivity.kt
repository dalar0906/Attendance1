package com.example.attendancecontrol.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.AttendanceRepository
import com.example.attendancecontrol.data.StudentRepository
import com.example.attendancecontrol.data.SubjectRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Экран студента: за выбранную дату видны ВСЕ предметы из списка,
 * по каждому можно выбрать статус. Кнопка «Сохранить» записывает
 * отметки по всем выбранным предметам одним действием.
 */
class StudentAttendanceActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
        const val EXTRA_DATE = "extra_date"
        /** Результат: список «дата|предмет», по которым отметки были сохранены. */
        const val EXTRA_SAVED_KEYS = "extra_saved_keys"
    }

    private lateinit var btnDate: Button
    private lateinit var etNewSubject: EditText
    private lateinit var tvEmpty: TextView
    private lateinit var lvSubjects: ListView
    private lateinit var btnSave: Button
    private lateinit var adapter: SubjectStatusAdapter

    private var studentId = -1
    private lateinit var date: String
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    private val subjects = mutableListOf<String>()
    private val statuses = mutableMapOf<String, String>()
    private var savedSnapshot: Map<String, String> = emptyMap()
    private val savedKeys = linkedSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_attendance)

        studentId = intent.getIntExtra(EXTRA_STUDENT_ID, -1)
        date = intent.getStringExtra(EXTRA_DATE) ?: dateFormat.format(Calendar.getInstance().time)

        val student = StudentRepository.getById(studentId)
        if (student == null) {
            Toast.makeText(this, "Студент не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<TextView>(R.id.tvStudentInfo).text =
            "${student.lastName} ${student.firstName} • ${student.groupName}"
        btnDate = findViewById(R.id.btnDate)
        etNewSubject = findViewById(R.id.etNewSubject)
        tvEmpty = findViewById(R.id.tvEmpty)
        lvSubjects = findViewById(R.id.lvSubjects)
        btnSave = findViewById(R.id.btnSave)

        adapter = SubjectStatusAdapter(this, subjects, statuses) { }
        lvSubjects.adapter = adapter

        loadForDate()

        btnDate.setOnClickListener { askChangeDate() }
        findViewById<Button>(R.id.btnAddSubject).setOnClickListener { addSubject() }
        btnSave.setOnClickListener { save() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { handleBack() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = handleBack()
        })
    }

    /** Загружает список предметов и уже сохранённые отметки студента за текущую дату. */
    private fun loadForDate() {
        btnDate.text = "Дата: $date"

        subjects.clear()
        subjects.addAll(SubjectRepository.getAll())

        statuses.clear()
        statuses.putAll(AttendanceRepository.getStudentStatuses(studentId, date))
        savedSnapshot = statuses.toMap()

        refreshList()
    }

    private fun refreshList() {
        adapter.notifyDataSetChanged()
        val empty = subjects.isEmpty()
        tvEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        lvSubjects.visibility = if (empty) View.GONE else View.VISIBLE
        btnSave.isEnabled = !empty
    }

    private fun addSubject() {
        val raw = etNewSubject.text.toString()
        if (!SubjectRepository.isValid(raw)) {
            Toast.makeText(this, "Введите предмет (2–50 символов)", Toast.LENGTH_SHORT).show()
            return
        }
        val subject = SubjectRepository.add(raw)
        if (subjects.any { it.equals(subject, ignoreCase = true) }) {
            Toast.makeText(this, "Такой предмет уже есть в списке", Toast.LENGTH_SHORT).show()
            return
        }
        // Уже выбранные статусы остаются — меняется только список предметов
        subjects.clear()
        subjects.addAll(SubjectRepository.getAll())
        etNewSubject.text.clear()
        refreshList()
        Toast.makeText(this, "Предмет добавлен", Toast.LENGTH_SHORT).show()
    }

    private fun askChangeDate() {
        val cal = Calendar.getInstance()
        dateFormat.parse(date)?.let { cal.time = it }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                val newDate = dateFormat.format(cal.time)
                if (newDate != date) {
                    if (hasUnsavedChanges()) {
                        AlertDialog.Builder(this)
                            .setTitle("Несохранённые изменения")
                            .setMessage("Отметки за $date не сохранены. Сменить дату без сохранения?")
                            .setPositiveButton("Сменить") { _, _ -> date = newDate; loadForDate() }
                            .setNegativeButton("Остаться", null)
                            .show()
                    } else {
                        date = newDate
                        loadForDate()
                    }
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        if (statuses.isEmpty()) {
            Toast.makeText(this, "Выберите статус хотя бы по одному предмету", Toast.LENGTH_SHORT).show()
            return
        }
        // Одним действием записываем отметки по всем предметам, где выбран статус
        AttendanceRepository.saveStudentMarks(studentId, date, statuses)
        statuses.keys.forEach { savedKeys.add("$date|$it") }
        savedSnapshot = statuses.toMap()

        setResult(RESULT_OK, Intent().putStringArrayListExtra(EXTRA_SAVED_KEYS, ArrayList(savedKeys)))
        Toast.makeText(this, "Сохранено предметов: ${statuses.size}", Toast.LENGTH_SHORT).show()
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
