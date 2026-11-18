package com.example.attendancecontrol.data

import com.example.attendancecontrol.AttendanceControlApp

/**
 * Сохраняет прежний интерфейс Sprint 1–3, но вместо MutableList использует Room.
 * Поэтому существующие Activity можно не переписывать.
 */
object StudentRepository {
    private val dao get() = AttendanceControlApp.database.studentDao()

    fun getAll(): List<Student> = dao.getAll().map { it.toStudent() }

    fun getById(id: Int): Student? = dao.getById(id)?.toStudent()

    fun getByGroup(groupName: String): List<Student> =
        dao.getByGroup(groupName).map { it.toStudent() }

    fun getGroups(): List<String> = dao.getGroups()

    fun add(firstName: String, lastName: String, groupName: String): Student {
        val cleanFirst = firstName.trim()
        val cleanLast = lastName.trim()
        val cleanGroup = groupName.trim()

        val existing = getAll().firstOrNull {
            it.firstName.equals(cleanFirst, true) &&
                    it.lastName.equals(cleanLast, true) &&
                    it.groupName.equals(cleanGroup, true)
        }
        if (existing != null) return existing

        val id = dao.insert(
            StudentRoomEntity(0, cleanFirst, cleanLast, cleanGroup)
        ).toInt()

        return getById(id)
            ?: throw IllegalStateException("Не удалось сохранить студента")
    }

    fun delete(student: Student) {
        // Внешний ключ Room удалит связанные отметки автоматически.
        dao.deleteById(student.id)
    }

    fun exists(firstName: String, lastName: String, groupName: String): Boolean =
        dao.exists(firstName.trim(), lastName.trim(), groupName.trim())

    private fun StudentRoomEntity.toStudent() =
        Student(id = id, firstName = firstName, lastName = lastName, groupName = groupName)
}
