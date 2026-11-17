package com.example.attendancecontrol.data

/**
 * Одна итоговая отметка посещаемости: студент + занятие (дата + предмет).
 */
data class Attendance(
    val studentId: Int,
    val lessonDate: String,
    val subject: String,
    val status: String
)
