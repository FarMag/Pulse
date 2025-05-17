package com.example.vkr_pulse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class CheckUserAuthorizationActivity : AppCompatActivity() {

    private lateinit var myPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        checkAccessToken()
    }

    private fun checkAccessToken() {
        val accessToken = myPrefs.getString("access_jwt", null)
        if (accessToken != null) {
            verifyAccessToken(accessToken)
        } else {
            // Переход на MainActivity, если токен отсутствует
            navigateToMainActivity()
        }
    }

    private fun verifyAccessToken(accessToken: String) {
        val url = getString(R.string.url_auth) + "checkUserAuthorization"
        val client = OkHttpClient()
        val formBody = FormBody.Builder().add("access_token", accessToken).build()
        val request = Request.Builder().url(url).post(formBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                showToast("Ошибка получения данных")
                navigateToMainActivity()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    handleResponse(response)
                }
            }

            private fun handleResponse(response: Response) {
                if (!response.isSuccessful) {
                    navigateToMainActivity() // Переход на MainActivity, если токен не актуален
                    return
                }

                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        if (jsonResponse.getBoolean("isValid")) {
//                            navigateToMainHomeActivity()
                            addTodayProgress(accessToken)
                        } else {
                            showToast("Токен истек")
                            navigateToMainActivity()
                        }
                    } catch (e: JSONException) {
                        showToast("Ошибка разбора данных")
                        navigateToMainActivity()
                    }
                } else {
                    showToast("Пустой ответ от сервера")
                    navigateToMainActivity()
                }
            }
        })
    }

    private fun addTodayProgress(accessToken: String) {
        val url = getString(R.string.url_progress) + "addTodayProgress"
        val client = OkHttpClient()
        val formBody = FormBody.Builder().add("access_token", accessToken).build()
        val request = Request.Builder().url(url).post(formBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                showToast("Ошибка получения данных")
                navigateToMainActivity()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    handleResponse(response)
                }
            }

            private fun handleResponse(response: Response) {
                if (!response.isSuccessful) {
                    showToast("Ошибка: ${response.code}")
                    navigateToMainActivity()
                    return
                }

                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val message = jsonResponse.getString("message")

                        when (response.code) {
                            200 -> {
//                                showToast(message) // Тут можно информировать пользователя
                                navigateToMainHomeActivity()
                            }
                            201 -> {
                                // Успешное создание новой записи
//                                showToast(message)
                                navigateToMainHomeActivity()
                            }
                            400 -> {
                                // Обработка ошибки Bad Request
//                                showToast("Неверный запрос: $message")
                                navigateToMainActivity()
                            }
                            500 -> {
                                // Обработка ошибки сервера
//                                showToast("Ошибка сервера: $message")
                                navigateToMainActivity()
                            }
                            else -> {
                                showToast("Неизвестный ответ от сервера")
                                navigateToMainActivity()
                            }
                        }
                    } catch (e: JSONException) {
                        showToast("Ошибка разбора данных")
                        navigateToMainActivity()
                    }
                } else {
                    showToast("Пустой ответ от сервера")
                    navigateToMainActivity()
                }
            }
        })
    }



    private fun navigateToMainHomeActivity() {
        val intent = Intent(this, MainHomeActivity::class.java) // или используйте FragmentManager
        startActivity(intent)
        finish() // Закрываем текущую Activity
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

