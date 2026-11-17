package com.example.attendancecontrol.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.RadioGroup
import android.widget.TextView
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.AttendanceStatus

/**
 * Адаптер для экрана студента: строка = предмет, в строке RadioGroup со статусом.
 * Если по предмету ничего не выбрано — отметки нет (в [statuses] нет ключа).
 */
class SubjectStatusAdapter(
    private val context: Context,
    private val subjects: List<String>,
    val statuses: MutableMap<String, String>,
    private val onStatusChanged: () -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = subjects.size
    override fun getItem(position: Int): String = subjects[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.item_subject_status, parent, false)

        val subject = subjects[position]
        view.findViewById<TextView>(R.id.tvSubjectName).text = subject
        val rgStatus = view.findViewById<RadioGroup>(R.id.rgStatus)

        rgStatus.setOnCheckedChangeListener(null)
        when (statuses[subject]) {
            AttendanceStatus.PRESENT -> rgStatus.check(R.id.rbPresent)
            AttendanceStatus.ABSENT -> rgStatus.check(R.id.rbAbsent)
            AttendanceStatus.LATE -> rgStatus.check(R.id.rbLate)
            else -> rgStatus.clearCheck()
        }
        rgStatus.setOnCheckedChangeListener { _, checkedId ->
            val status = when (checkedId) {
                R.id.rbPresent -> AttendanceStatus.PRESENT
                R.id.rbAbsent -> AttendanceStatus.ABSENT
                R.id.rbLate -> AttendanceStatus.LATE
                else -> null
            }
            if (status != null) {
                statuses[subject] = status
                onStatusChanged()
            }
        }
        return view
    }
}
