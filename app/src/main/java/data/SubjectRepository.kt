package com.example.attendancecontrol.data

object SubjectRepository {

    private val subjects = mutableListOf<String>()

    fun getAll(): List<String> = subjects.sorted()

    fun isValid(raw: String): Boolean {
        val subject = raw.trim()
        return subject.length in 2..50
    }

    /**
     * Добавляет предмет. Если такой уже есть (без учёта регистра) — возвращает существующий.
     */
    fun add(raw: String): String {
        val subject = raw.trim()
        val existing = subjects.find { it.equals(subject, ignoreCase = true) }
        if (existing != null) return existing

        subjects.add(subject)
        return subject
    }

    fun clear() {
        subjects.clear()
    }
}