package com.example.attendancecontrol.data

object StudentRepository {
    private val students = mutableListOf<Student>()
    private var nextId = 1

    fun getAll(): List<Student> = students.toList()

    fun getById(id: Int): Student? = students.find { it.id == id }

    /** Студенты одной группы, отсортированные по фамилии. */
    fun getByGroup(groupName: String): List<Student> =
        students.filter { it.groupName.equals(groupName, ignoreCase = true) }
            .sortedWith(compareBy({ it.lastName.lowercase() }, { it.firstName.lowercase() }))

    /** Список групп без повторов (без учёта регистра), по алфавиту. */
    fun getGroups(): List<String> =
        students.map { it.groupName }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

    fun add(firstName: String, lastName: String, groupName: String): Student {
        val student = Student(
            id = nextId++,
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            groupName = groupName.trim()
        )
        students.add(student)
        return student
    }

    fun delete(student: Student) {
        students.removeAll { it.id == student.id }
        AttendanceRepository.removeForStudent(student.id)
    }


    fun exists(firstName: String, lastName: String, groupName: String): Boolean {
        return students.any {
            it.firstName.equals(firstName.trim(), ignoreCase = true) &&
                    it.lastName.equals(lastName.trim(), ignoreCase = true) &&
                    it.groupName.equals(groupName.trim(), ignoreCase = true)
        }
    }
}