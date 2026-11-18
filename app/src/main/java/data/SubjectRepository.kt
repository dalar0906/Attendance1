package com.example.attendancecontrol.data

import com.example.attendancecontrol.AttendanceControlApp

import java.util.Locale

/** Справочник предметов также сохраняется между запусками приложения. */
object SubjectRepository {
    private val dao get() = AttendanceControlApp.database.subjectDao()

    fun getAll(): List<String> = dao.getAll()

    fun isValid(raw: String): Boolean {
        val subject = raw.trim()
        return subject.length in 2..50
    }

    /** Добавляет предмет; дубликаты без учёта регистра возвращают исходное написание. */
    fun add(raw: String): String {
        val displayName = raw.trim()
        val normalized = displayName.lowercase(Locale.ROOT)

        val existing = dao.getByNormalizedName(normalized)
        if (existing != null) return existing

        dao.insert(SubjectRoomEntity(normalized, displayName))
        return dao.getByNormalizedName(normalized) ?: displayName
    }

    fun clear() {
        // Намеренно не очищаем постоянный справочник при обычной работе приложения.
        // Метод оставлен для совместимости с прежним API.
    }
}
