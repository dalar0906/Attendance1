package com.example.attendancecontrol.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.attendancecontrol.R
import com.example.attendancecontrol.data.Student
import com.example.attendancecontrol.data.StudentRepository

class StudentsActivity : AppCompatActivity() {

    private lateinit var etLastName: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etGroup: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnBack: Button
    private lateinit var lvStudents: ListView
    private lateinit var tvEmpty: TextView

    private lateinit var adapter: ArrayAdapter<String>
    private val displayList = mutableListOf<String>()
    private val studentsCache = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_students)

        etLastName = findViewById(R.id.etLastName)
        etFirstName = findViewById(R.id.etFirstName)
        etGroup = findViewById(R.id.etGroup)
        btnAdd = findViewById(R.id.btnAddStudent)
        btnBack = findViewById(R.id.btnBack)
        lvStudents = findViewById(R.id.lvStudents)
        tvEmpty = findViewById(R.id.tvEmpty)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayList)
        lvStudents.adapter = adapter

        // Кнопка Назад
        btnBack.setOnClickListener {
            finish()
        }

        // Добавление
        btnAdd.setOnClickListener {
            addStudent()
        }

        // ===== УДАЛЕНИЕ =====
        // Нажали на студента → появляется диалог подтверждения
        lvStudents.setOnItemClickListener { _, _, position, _ ->
            val student = studentsCache[position]

            AlertDialog.Builder(this)
                .setTitle("Удаление студента")
                .setMessage("Удалить студента:\n${student.lastName} ${student.firstName}\nГруппа: ${student.groupName}?")
                .setPositiveButton("Удалить") { _, _ ->
                    StudentRepository.delete(student)
                    Toast.makeText(this, "Студент удалён", Toast.LENGTH_SHORT).show()
                    refreshList()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        refreshList()
    }

    private fun addStudent() {
        val lastName = etLastName.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val group = etGroup.text.toString().trim()

        // 1. Пустые поля
        if (lastName.isEmpty() || firstName.isEmpty() || group.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Проверка фамилии и имени
        // Разрешены: буквы (рус + англ), пробелы, все виды дефисов
        val nameRegex = Regex("^[А-Яа-яЁёA-Za-z\\s\\-–—]+$")

        if (!lastName.matches(nameRegex) || lastName.length < 2) {
            Toast.makeText(this, "Фамилия: только буквы (минимум 2 символа)", Toast.LENGTH_SHORT).show()
            return
        }
        if (!firstName.matches(nameRegex) || firstName.length < 2) {
            Toast.makeText(this, "Имя: только буквы (минимум 2 символа)", Toast.LENGTH_SHORT).show()
            return
        }

        // Дополнительно: не должно состоять только из пробелов и дефисов
        val onlyLettersCheck = Regex("[А-Яа-яЁёA-Za-z]")
        if (!lastName.contains(onlyLettersCheck) || !firstName.contains(onlyLettersCheck)) {
            Toast.makeText(this, "Имя и фамилия должны содержать буквы", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. Проверка группы
        val groupRegex = Regex("^[А-Яа-яЁёA-Za-z0-9\\-–—]+$")
        if (!group.matches(groupRegex) || group.length < 2) {
            Toast.makeText(this, "Группа: только буквы, цифры и дефис", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. Дубликат
        if (StudentRepository.exists(firstName, lastName, group)) {
            Toast.makeText(this, "Такой студент уже есть в списке", Toast.LENGTH_SHORT).show()
            return
        }

        // Всё ок
        StudentRepository.add(firstName, lastName, group)
        Toast.makeText(this, "Студент добавлен", Toast.LENGTH_SHORT).show()

        etLastName.text.clear()
        etFirstName.text.clear()
        etGroup.text.clear()
        refreshList()
    }

    private fun refreshList() {
        studentsCache.clear()
        studentsCache.addAll(StudentRepository.getAll())

        displayList.clear()
        studentsCache.forEach {
            displayList.add("${it.lastName} ${it.firstName} — ${it.groupName}")
        }
        adapter.notifyDataSetChanged()

        if (displayList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            lvStudents.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            lvStudents.visibility = View.VISIBLE
        }
    }
}