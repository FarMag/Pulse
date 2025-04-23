package com.example.vkr_pulse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.example.vkr_pulse.AuthHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class LoginActivity : AppCompatActivity() {

    private val okhttpclient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)

        // Обработка кнопки "Войти"
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val username = emailField.text.toString()
            val password = passwordField.text.toString()

            when {
                username.isEmpty() -> Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show()
                else -> login(username, password)
//                authenticateUser("login", email = "user@example.com", password = "password123")
            }

        }

        // Переход к регистрации
        val registerButton: Button = findViewById(R.id.registerButton) // Замените на ваш ID кнопки
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }

        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun login(email: String, password: String) {
        val url = getString(R.string.url_auth) + "login"
        val formBody = FormBody.Builder()
            .add("email", email)
            .add("password_hash", password)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        okhttpclient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logAndShowToast("Ошибка входа", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
//                    if (!response.isSuccessful) {
//                        logAndShowToast("Неверный ответ: $response")
//                        return
//                    }

                    val responseData = response.body?.string()
                    handleResponse(responseData)
                }
            }

            private fun handleResponse(responseData: String?) {
                runOnUiThread {
                    if (responseData != null) {
                        try {
                            val jsonResponse = JSONObject(responseData)
                            when {
                                jsonResponse.has("access_token") && jsonResponse.has("refresh_token") &&
                                        jsonResponse.getString("message") == "success" -> {
                                    val accessToken = jsonResponse.getString("access_token")
                                    val refreshToken = jsonResponse.getString("refresh_token")
                                    saveTokens(accessToken, refreshToken)
                                    navigateToMainHomeActivity()
                                }
                                jsonResponse.has("access_token") && jsonResponse.has("refresh_token") &&
                                        jsonResponse.getString("message") == "incomplete data" -> {
                                    val accessToken = jsonResponse.getString("access_token")
                                    val refreshToken = jsonResponse.getString("refresh_token")
                                    saveTokens(accessToken, refreshToken)
                                    navigateToOnboardingQuizActivity()
                                }
                                else -> {
                                    handleErrorResponse(responseData)
                                }
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(this@LoginActivity, "Ошибка обработки ответа от сервера", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            private fun handleErrorResponse(responseData: String) {
                val errorMessage = when {
                    responseData.contains("All fields are required") -> "Заполните все поля"
                    responseData.contains("Email or Password wrong") -> "Неверная почта или пароль"
                    else -> "Ошибка: $responseData"
                }
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

            private fun saveTokens(accessToken: String, refreshToken: String) {
                val preferences = getSharedPreferences("myPrefs", MODE_PRIVATE)
                preferences.edit().apply {
                    putString("access_jwt", accessToken)
                    putString("refresh_jwt", refreshToken)
                    apply()
                }
                showSuccessToast("Вы успешно вошли в аккаунт")
            }


            private fun navigateToMainHomeActivity() {
                val intent = Intent(this@LoginActivity, MainHomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            }

            private fun navigateToOnboardingQuizActivity() {
                val intent = Intent(this@LoginActivity, OnboardingQuizActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            }


            private fun logAndShowToast(message: String, throwable: Throwable? = null) {
                Log.e("LoginActivity", message, throwable)
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    private fun navigateToMainHomeActivity() {
        val intent = Intent(this@LoginActivity, MainHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun navigateToOnboardingQuizActivity() {
        val intent = Intent(this@LoginActivity, OnboardingQuizActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

    private fun showSuccessToast(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.layout_success_toast, null)

        val toastText = layout.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        val toast = Toast(applicationContext)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 120)
        toast.show()
    }


}
