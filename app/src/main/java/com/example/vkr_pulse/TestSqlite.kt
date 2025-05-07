package com.example.vkr_pulse

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "mydatabase.db", null, 1) {
    private val dbContext = context // Сохраняем контекст в переменной

    override fun onCreate(db: SQLiteDatabase) {
        // Создание таблиц
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, email TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Обновление базы данных
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // Метод для вставки тестовых данных в таблицу "users"
    fun insertTestData() {
        val db = writableDatabase

        val testData = arrayOf(
            Pair("user1", "user1@example.com"),
            Pair("user2", "user2@example.com"),
            Pair("user3", "user3@example.com")
        )

        for (data in testData) {
            val values = ContentValues().apply {
                put("username", data.first)
                put("email", data.second)
            }
            db.insert("users", null, values)
        }

        db.close()
    }

    // Метод для удаления базы данных
    fun deleteDatabase() {
        val dbFile = dbContext.getDatabasePath("mydatabase.db") // Используем сохранённый контекст
        if (dbFile.exists()) {
            dbFile.delete()
        }
    }
}

class TestSqlite : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_sqlite)

        databaseHelper = DatabaseHelper(this)

        val buttonAdd = findViewById<Button>(R.id.button_add)
        val buttonClear = findViewById<Button>(R.id.button_clear)

        // Установка обработчика для кнопки добавления данных
        buttonAdd.setOnClickListener {
            databaseHelper.insertTestData()
        }

        // Установка обработчика для кнопки очистки данных
        buttonClear.setOnClickListener {
            databaseHelper.deleteDatabase() // Удаляем базу данных
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.deleteDatabase() // Удаляем базу данных при закрытии приложения
    }
}
