package com.example.attendancecontrol.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioGroup
import android.widget.TextView
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.AttendanceStatus   // ← вот эту строку добавь
import com.example.attendancecontrol.data.Student

/**
 * Адаптер списка студентов: у каждого — RadioGroup с тремя статусами.
 * Текущий выбор хранится в [statuses], а не во View, поэтому он
 * не теряется при переиспользовании строк ListView.
 */
class AttendanceAdapter(
    private val context: Context,
    private val students: List<Student>,
    val statuses: MutableMap<Int, String>,
    private val onStatusChanged: () -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = students.size
    override fun getItem(position: Int): Student = students[position]
    override fun getItemId(position: Int): Long = students[position].id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_attendance_student, parent, false)

        val student = students[position]
        val tvName = view.findViewById<TextView>(R.id.tvStudentName)
        val rgStatus = view.findViewById<RadioGroup>(R.id.rgStatus)

        tvName.text = "${student.lastName} ${student.firstName}  ›"

        // Сначала снимаем слушатель, чтобы programmatic check() не считался действием пользователя
        rgStatus.setOnCheckedChangeListener(null)
        rgStatus.check(buttonIdFor(statuses[student.id] ?: AttendanceStatus.PRESENT))
        rgStatus.setOnCheckedChangeListener { _, checkedId ->
            statuses[student.id] = statusFor(checkedId)
            onStatusChanged()
        }
        return view
    }

    private fun buttonIdFor(status: String): Int = when (status) {
        AttendanceStatus.ABSENT -> R.id.rbAbsent
        AttendanceStatus.LATE -> R.id.rbLate
        else -> R.id.rbPresent
    }

    private fun statusFor(buttonId: Int): String = when (buttonId) {
        R.id.rbAbsent -> AttendanceStatus.ABSENT
        R.id.rbLate -> AttendanceStatus.LATE
        else -> AttendanceStatus.PRESENT
    }
}
