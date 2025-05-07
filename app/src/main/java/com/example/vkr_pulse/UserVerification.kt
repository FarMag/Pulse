package com.example.vkr_pulse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

// Модель данных для пользователя
data class UserData(
    val name: String,
    val email: String,
    val age: Int,
    val weight: Int
)

// Основной класс активности
class UserVerification : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val client = OkHttpClient()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        checkUserRegistration()
    }

    private fun checkUserRegistration() {
        // Проверяем, есть ли переменная "sub" в SharedPreferences
        val userId = sharedPreferences.getString("sub", null)

        // Если пользователь зарегистрирован
        if (userId != null) {
            loadDataFromServer(userId)
        } else {
            // Пользователь не зарегистрирован, переходим на MainActivity
            // Здесь можно добавить код для перехода на другую активность, если нужно
            // Например:
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loadDataFromServer(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Создание запроса
//                val request = Request.Builder()
//                    .url(getString(R.string.url_auth) + "userVerification") // Убедитесь, что URL правильный
//                    .build()

                val url = getString(R.string.url_auth) + "userVerification"
                val formBody = FormBody.Builder()
                    .add("id", userId)
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build()

                // Выполнение запроса
                val response: Response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // Парсинг данных
                val userData: UserData? = responseData?.let { parseUserData(it) }

                if (userData != null) {
                    saveDataTemporarily(userData)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parseUserData(jsonData: String): UserData? {
        return try {
            gson.fromJson(jsonData, UserData::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    private fun saveDataTemporarily(userData: UserData) {
        // Здесь вы можете временно сохранить данные в памяти, например, в ViewModel или другом объекте
        // Например, это можно сделать как-то так:
        val temporaryUserData = mapOf(
            "name" to userData.name,
            "email" to userData.email,
            "age" to userData.age
        )
        // Эта переменная будет хранить данные до тех пор, пока приложение не закроется
    }

    override fun onDestroy() {
        super.onDestroy()
        clearData()
    }

    private fun clearData() {
        // Если необходимо, можно очистить данные здесь
        // Например, если вы храните временные данные, вы можете сбросить их
    }
}
