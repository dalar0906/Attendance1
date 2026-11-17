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
        val cleanSubject = SubjectRepository.add(subject)
        records.removeAll { isSameLesson(it, lessonDate, cleanSubject) && it.studentId in marks }
        marks.forEach { (studentId, status) ->
            records.add(Attendance(studentId, lessonDate, cleanSubject, status))
        }
    }

    /**
     * Отметки одного студента за дату по всем предметам: предмет -> статус.
     * Ключи — написание предмета из [SubjectRepository].
     */
    fun getStudentStatuses(studentId: Int, lessonDate: String): Map<String, String> {
        return records
            .filter { it.studentId == studentId && it.lessonDate == lessonDate }
            .associate { SubjectRepository.add(it.subject) to it.status }
    }

    /**
     * Сохраняет отметки одного студента сразу по нескольким предметам за дату.
     * По каждому предмету остаётся одна итоговая отметка (прежняя заменяется).
     */
    fun saveStudentMarks(studentId: Int, lessonDate: String, marks: Map<String, String>) {
        marks.forEach { (subject, status) ->
            val clean = SubjectRepository.add(subject)
            records.removeAll {
                it.studentId == studentId && it.lessonDate == lessonDate &&
                        it.subject.equals(clean, ignoreCase = true)
            }
            records.add(Attendance(studentId, lessonDate, clean, status))
        }
    }

    // ДОБАВЛЕНО: полная замена всех отметок студента за дату.
    // Удаляет ВСЕ прежние записи студента за эту дату и записывает только переданные.
    // Нужно, чтобы предметы, у которых статус снят (null), не «зависали» в хранилище.
    fun replaceStudentMarks(studentId: Int, lessonDate: String, marks: Map<String, String>) {
        records.removeAll { it.studentId == studentId && it.lessonDate == lessonDate }
        marks.forEach { (subject, status) ->
            val clean = SubjectRepository.add(subject)
            records.add(Attendance(studentId, lessonDate, clean, status))
        }
    }

    /** Удаляет отметки студента (вызывается при удалении студента). */
    fun removeForStudent(studentId: Int) {
        records.removeAll { it.studentId == studentId }
    }

    private fun isSameLesson(a: Attendance, lessonDate: String, subject: String): Boolean =
        a.lessonDate == lessonDate && a.subject.equals(subject.trim(), ignoreCase = true)
}