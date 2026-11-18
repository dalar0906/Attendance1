package com.example.attendancecontrol.data

import com.example.attendancecontrol.AttendanceControlApp
/**
 * Постоянное хранилище отметок через Room.
 * Сохраняется правило: одна итоговая отметка на студента, дату и предмет.
 */
object AttendanceRepository {
    private val database get() = AttendanceControlApp.database
    private val dao get() = database.attendanceDao()

    fun getAll(): List<Attendance> = dao.getAll().map { it.toAttendance() }

    fun getStatuses(lessonDate: String, subject: String): Map<Int, String> =
        dao.getForLesson(lessonDate, subject).associate { it.studentId to it.status }

    fun saveLesson(lessonDate: String, subject: String, marks: Map<Int, String>) {
        val cleanSubject = SubjectRepository.add(subject)
        if (marks.isEmpty()) return

        val records = marks.map { (studentId, status) ->
            AttendanceRoomEntity(studentId, lessonDate, cleanSubject, status)
        }

        database.runInTransaction {
            dao.deleteForLessonStudents(lessonDate, cleanSubject, marks.keys.toList())
            dao.insertAll(records)
        }
    }

    fun getStudentStatuses(studentId: Int, lessonDate: String): Map<String, String> =
        dao.getForStudentAndDate(studentId, lessonDate)
            .associate { SubjectRepository.add(it.subject) to it.status }

    fun saveStudentMarks(studentId: Int, lessonDate: String, marks: Map<String, String>) {
        if (marks.isEmpty()) return
        database.runInTransaction {
            marks.forEach { (subject, status) ->
                val clean = SubjectRepository.add(subject)
                dao.deleteForStudentDateSubject(studentId, lessonDate, clean)
                dao.insertAll(
                    listOf(AttendanceRoomEntity(studentId, lessonDate, clean, status))
                )
            }
        }
    }

    /** Полная замена всех отметок студента за выбранную дату. */
    fun replaceStudentMarks(studentId: Int, lessonDate: String, marks: Map<String, String>) {
        val records = marks.map { (subject, status) ->
            val clean = SubjectRepository.add(subject)
            AttendanceRoomEntity(studentId, lessonDate, clean, status)
        }

        database.runInTransaction {
            dao.deleteForStudentAndDate(studentId, lessonDate)
            if (records.isNotEmpty()) dao.insertAll(records)
        }
    }

    fun removeForStudent(studentId: Int) {
        dao.deleteForStudent(studentId)
    }

    private fun AttendanceRoomEntity.toAttendance() =
        Attendance(studentId = studentId, lessonDate = lessonDate, subject = subject, status = status)
}
