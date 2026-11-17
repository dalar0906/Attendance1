package com.example.attendancecontrol.data

/**
 * Хранилище отметок посещаемости в памяти (текущий сеанс).
 * Правило: одна итоговая отметка на студента и занятие (дата + предмет).
 * Постоянное хранение (Room) — спринт 4.
 */
object AttendanceRepository {
    private val records = mutableListOf<Attendance>()

    fun getAll(): List<Attendance> = records.toList()

    /** Ранее сохранённые статусы студентов для данного занятия: studentId -> статус. */
    fun getStatuses(lessonDate: String, subject: String): Map<Int, String> {
        return records
            .filter { isSameLesson(it, lessonDate, subject) }
            .associate { it.studentId to it.status }
    }

    /**
     * Сохраняет отметки занятия. Повторное сохранение заменяет прежние отметки
     * (у студента остаётся одна итоговая отметка на занятие).
     */
    fun saveLesson(lessonDate: String, subject: String, marks: Map<Int, String>) {
        val cleanSubject = subject.trim()
        records.removeAll { isSameLesson(it, lessonDate, cleanSubject) && it.studentId in marks }
        marks.forEach { (studentId, status) ->
            records.add(Attendance(studentId, lessonDate, cleanSubject, status))
        }
    }

    /** Удаляет отметки студента (вызывается при удалении студента). */
    fun removeForStudent(studentId: Int) {
        records.removeAll { it.studentId == studentId }
    }

    private fun isSameLesson(a: Attendance, lessonDate: String, subject: String): Boolean =
        a.lessonDate == lessonDate && a.subject.equals(subject.trim(), ignoreCase = true)
}
