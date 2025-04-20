package com.example.vkr_pulse

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vkr_pulse.data.User
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private val okhttpclient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val passwordAgainEditText = findViewById<EditText>(R.id.passwordEditText_again)
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val genderRadioGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)

        // Добавляем TextWatcher для форматирования и валидации даты рождения
        ageEditText.addTextChangedListener(object : TextWatcher {
            var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                isUpdating = true

                // Удаляем все точки из строки
                val cleaned = s.toString().replace(".", "")

                // Ограничиваем ввод до 8 символов
                if (cleaned.length > 8) {
                    ageEditText.setText(cleaned.substring(0, 8))
                    ageEditText.setSelection(8) // Устанавливаем курсор в конец
                    isUpdating = false
                    return
                }

                // Форматируем дату в виде DD.MM.YYYY
                val formatted = StringBuilder()
                for (i in cleaned.indices) {
                    if (i == 2 || i == 4) {
                        formatted.append(".")
                    }
                    formatted.append(cleaned[i])
                }

                ageEditText.setText(formatted.toString())
                ageEditText.setSelection(formatted.length) // Устанавливаем курсор в конец
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        // Кнопка регистрации
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val passwordAgain = passwordAgainEditText.text.toString()
            val birthDate = ageEditText.text.toString()

            val selectedGenderId = genderRadioGroup.checkedRadioButtonId
            val gender = when (selectedGenderId) {
                R.id.maleRadioButton -> "male"
                R.id.femaleRadioButton -> "female"
                else -> null // Если пол не выбран
            }

            when {
                username.isEmpty() -> Toast.makeText(this, "Введите имя", Toast.LENGTH_SHORT).show()
                !isValidEmail(email) -> Toast.makeText(this, "Введите корректную почту", Toast.LENGTH_SHORT).show()
                password != passwordAgain -> Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                gender == null -> Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
                !isValidDate(birthDate) -> Toast.makeText(this, "Введите корректную дату рождения", Toast.LENGTH_SHORT).show()
                else -> register(username, email, password, birthDate, gender)
            }
        }
    }

    // Метод для проверки корректности почты
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    // Метод для проверки даты рождения
    private fun isValidDate(date: String): Boolean {
        // Проверка формата
        if (!Regex("""\d{2}\.\d{2}\.\d{4}""").matches(date)) {
            return false
        }

        val day = date.substring(0, 2).toInt()
        val month = date.substring(3, 5).toInt()
        val year = date.substring(6, 10).toInt()


        // Проверка на валидность дня, месяца и года
        if (day !in 1..31 || month !in 1..12 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            return false
        }


        // Проверка существования даты
        return try {
            val dateFormat = SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())
            dateFormat.isLenient = false
            val parsedDate = dateFormat.parse(date)
            val currentDate = Calendar.getInstance().time
            parsedDate != null && !parsedDate.after(currentDate)
        } catch (e: Exception) {
            false
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainHomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun navigateToQuiz(user: User) {
        val intent = Intent(this, OnboardingQuizActivity::class.java).apply {
            putExtra("username", user.username)
            putExtra("email", user.email)
            putExtra("password", user.password)
            putExtra("birth_date", user.birth_date)
            putExtra("gender", user.gender)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun register(username: String, email: String, password: String, birthDate: String, gender: String) {
        val url = getString(R.string.url) + "register"
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("email", email)
            .add("password_hash", password)
            .add("birth_date", birthDate)
            .add("gender", gender)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        okhttpclient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logAndShowToast("Ошибка регистрации", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        logAndShowToast("Неожиданный код ответа: $response")
                        return
                    }

                    val responseData = response.body?.string()
                    handleResponse(responseData)
                }
            }

            private fun handleResponse(responseData: String?) {
                runOnUiThread {
                    responseData?.let {
                        try {
                            val jsonResponse = JSONObject(it)
                            when {
                                jsonResponse.has("message") && jsonResponse.getString("message") == "Success" -> {
                                    showSuccessToast("Аккаунт успешно создан!")
                                    val accessToken = jsonResponse.getString("access_token")
                                    val refreshToken = jsonResponse.getString("refresh_token")
                                    saveTokens(accessToken, refreshToken)

                                    val user = User(
                                        username = username,
                                        email = email,
                                        password = password,
                                        birth_date = birthDate,
                                        gender = gender
                                    )

                                    navigateToQuiz(user)
                                }
                                jsonResponse.has("message") && jsonResponse.getString("message") == "This email is already exists" -> {
                                    Toast.makeText(this@RegisterActivity, "Данный пользователь уже существует", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    handleErrorResponse(it)
                                }
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(this@RegisterActivity, "Ошибка обработки ответа от сервера", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }



            private fun handleErrorResponse(responseData: String) {
                val errorMessage = when {
                    responseData.contains("All fields are required") -> "Заполните все поля"
                    responseData.contains("This email is already exists") -> "Данная почта уже существует"
                    responseData.contains("Error: Database connection") -> "Ошибка подключения к базе данных"
                    else -> "Ошибка: $responseData"
                }
                Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

            private fun showSuccessToast(message: String) {
                val inflater = layoutInflater
                val layout = inflater.inflate(R.layout.layout_success_toast, null)

                val toastText = layout.findViewById<TextView>(R.id.toastText)
                toastText.text = message

                Toast(this@RegisterActivity).apply {
                    duration = Toast.LENGTH_SHORT
                    view = layout
                    setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 120)
                    show()
                }
            }



            private fun saveTokens(accessToken: String, refreshToken: String) {
                val preferences = getSharedPreferences("myPrefs", MODE_PRIVATE)
                preferences.edit().apply {
                    putString("access_jwt", accessToken)
                    putString("refresh_jwt", refreshToken)
                    apply()
                }
//                Toast.makeText(this@RegisterActivity, "Вход успешен", Toast.LENGTH_SHORT).show()
            }


            private fun navigateToLoginActivity() {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            private fun logAndShowToast(message: String, throwable: Throwable? = null) {
                Log.e("RegisterActivity", message, throwable)
                runOnUiThread {
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

}
