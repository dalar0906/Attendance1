package com.example.attendancecontrol.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.Attendance
import com.example.attendancecontrol.data.AttendanceRepository
import com.example.attendancecontrol.data.Student
import com.example.attendancecontrol.data.StudentRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** История посещаемости и статистика с фильтрами по группе, студенту и периоду. */
class StatisticsActivity : AppCompatActivity() {
    private lateinit var groupSpinner: Spinner
    private lateinit var studentSpinner: Spinner
    private lateinit var fromButton: Button
    private lateinit var toButton: Button
    private lateinit var lessonsView: TextView
    private lateinit var totalView: TextView
    private lateinit var presentView: TextView
    private lateinit var absentView: TextView
    private lateinit var lateView: TextView
    private lateinit var emptyView: TextView
    private lateinit var historyView: TextView

    private val format = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).apply {
        isLenient = false
    }
    private var students: List<Student> = emptyList()
    private var records: List<Attendance> = emptyList()
    private var startDate: String? = null
    private var endDate: String? = null
    private var changingSpinners = false

    private val selectedGroup: String?
        get() = groupSpinner.selectedItem?.toString()?.takeUnless { it == "Все группы" }

    private val filteredStudents: List<Student>
        get() = students.filter { selectedGroup == null || it.groupName == selectedGroup }
            .sortedWith(compareBy<Student> { it.lastName.lowercase() }.thenBy { it.firstName.lowercase() })

    private val selectedStudentId: Int?
        get() = studentSpinner.selectedItemPosition.takeIf { it > 0 }
            ?.let { filteredStudents.getOrNull(it - 1)?.id }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        groupSpinner = findViewById(R.id.spStatisticsGroup)
        studentSpinner = findViewById(R.id.spStatisticsStudent)
        fromButton = findViewById(R.id.btnDateFrom)
        toButton = findViewById(R.id.btnDateTo)
        lessonsView = findViewById(R.id.tvStatLessons)
        totalView = findViewById(R.id.tvStatTotal)
        presentView = findViewById(R.id.tvStatPresent)
        absentView = findViewById(R.id.tvStatAbsent)
        lateView = findViewById(R.id.tvStatLate)
        emptyView = findViewById(R.id.tvStatisticsEmpty)
        historyView = findViewById(R.id.tvStatisticsHistory)

        findViewById<Button>(R.id.btnStatisticsBack).setOnClickListener { finish() }
        fromButton.setOnClickListener { chooseDate(true) }
        toButton.setOnClickListener { chooseDate(false) }
        findViewById<Button>(R.id.btnResetFilters).setOnClickListener {
            startDate = null
            endDate = null
            updateDateButtons()
            updateResults()
        }

        groupSpinner.onItemSelectedListener = selectionListener {
            if (!changingSpinners) {
                updateStudentSpinner()
                updateResults()
            }
        }
        studentSpinner.onItemSelectedListener = selectionListener {
            if (!changingSpinners) updateResults()
        }
    }

    override fun onResume() {
        super.onResume()
        students = StudentRepository.getAll()
        records = AttendanceRepository.getAll()
        changingSpinners = true
        groupSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Все группы") + students.map { it.groupName }.distinct().sorted()
        )
        updateStudentSpinner()
        changingSpinners = false
        updateDateButtons()
        updateResults()
    }

    private fun updateStudentSpinner() {
        val labels = listOf("Все студенты") + filteredStudents.map {
            "${it.lastName} ${it.firstName} (${it.groupName})"
        }
        studentSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, labels
        )
        studentSpinner.setSelection(0)
    }

    private fun chooseDate(isStart: Boolean) {
        val calendar = Calendar.getInstance()
        val existing = if (isStart) startDate else endDate
        existing?.let { runCatching { format.parse(it) }.getOrNull() }?.let {
            calendar.time = it
        }
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            val chosen = format.format(calendar.time)
            if (isStart) startDate = chosen else endDate = chosen
            if (startDate != null && endDate != null &&
                dateMillis(startDate!!) > dateMillis(endDate!!)
            ) {
                Toast.makeText(this, "Начальная дата позже конечной", Toast.LENGTH_LONG).show()
                if (isStart) startDate = null else endDate = null
            }
            updateDateButtons()
            updateResults()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun dateMillis(value: String): Long =
        runCatching { format.parse(value)?.time ?: 0L }.getOrDefault(0L)

    private fun updateDateButtons() {
        fromButton.text = startDate?.let { "С: $it" } ?: "Дата начала: любая"
        toButton.text = endDate?.let { "По: $it" } ?: "Дата окончания: любая"
    }

    private fun updateResults() {
        val group = selectedGroup
        val studentId = selectedStudentId
        val filtered = records.filter { record ->
            val owner = students.firstOrNull { it.id == record.studentId }
            val date = dateMillis(record.lessonDate)
            (group == null || owner?.groupName == group) &&
                    (studentId == null || record.studentId == studentId) &&
                    (startDate == null || date >= dateMillis(startDate!!)) &&
                    (endDate == null || date <= dateMillis(endDate!!))
        }.sortedByDescending { dateMillis(it.lessonDate) }

        val present = filtered.count { it.status == "Присутствовал" }
        val absent = filtered.count { it.status == "Отсутствовал" }
        val late = filtered.count { it.status == "Опоздал" }
        val lessonCount = filtered.map { "${it.lessonDate}|${it.subject}" }.distinct().size

        lessonsView.text = "Количество занятий: $lessonCount"
        totalView.text = "Всего отметок: ${filtered.size}"
        presentView.text = "Присутствовали: $present"
        absentView.text = "Отсутствовали: $absent"
        lateView.text = "Опоздали: $late"

        emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        historyView.text = filtered.joinToString("\n\n") { record ->
            val owner = students.firstOrNull { it.id == record.studentId }
            val name = owner?.let { "${it.lastName} ${it.firstName}" }
                ?: "Студент ID ${record.studentId}"
            "$name\nГруппа: ${owner?.groupName ?: "неизвестна"}\n" +
                    "${record.lessonDate} • ${record.subject}\nСтатус: ${record.status}"
        }
    }

    private fun selectionListener(action: () -> Unit) =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) = action()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
}
