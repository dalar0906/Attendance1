package com.example.attendancecontrol.data

/**
 * Список предметов (в памяти, текущий сеанс).
 * Предмет попадает в список, когда его вводят на экране занятия,
 * добавляют на экране студента или сохраняют по нему отметку.
 * Постоянное хранение (Room) — спринт 4.
 */
object SubjectRepository {
    private val subjects = mutableListOf<String>()

    const val MIN_LENGTH = 2
    const val MAX_LENGTH = 50

    /** Все предметы по алфавиту. */
    fun getAll(): List<String> = subjects.sortedBy { it.lowercase() }

    fun isValid(name: String): Boolean = normalize(name).length in MIN_LENGTH..MAX_LENGTH

    /**
     * Добавляет предмет, если его ещё нет (без учёта регистра и лишних пробелов).
     * Возвращает «официальное» написание предмета из списка.
     */
    fun add(name: String): String {
        val clean = normalize(name)
        val existing = subjects.firstOrNull { it.equals(clean, ignoreCase = true) }
        if (existing != null) return existing
        subjects.add(clean)
        return clean
    }

    private fun normalize(name: String): String = name.trim().replace(Regex("\\s+"), " ")
}
