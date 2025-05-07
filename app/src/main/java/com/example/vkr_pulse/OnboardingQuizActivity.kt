package com.example.vkr_pulse

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.auth0.android.jwt.JWT
import com.example.vkr_pulse.data.QuizAnswers
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

class OnboardingQuizActivity : AppCompatActivity(), OnAnswerSelectedListener {

    private val okhttpclient = OkHttpClient()
    private lateinit var sharedPreferences: SharedPreferences

    private val totalQuestions = 5
    private var currentQuestion = 1

    val quizAnswers = QuizAnswers()
    lateinit var user: User

    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var progressBar: ProgressBar

    private val fragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_quiz)

        sharedPreferences = getSharedPreferences("myPrefs", MODE_PRIVATE)

        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = totalQuestions

        val username = intent.getStringExtra("username") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        val birthDate = intent.getStringExtra("birth_date") ?: ""
        val gender = intent.getStringExtra("gender") ?: ""
        user = User(username, email, password, birthDate, gender)

        fragments.add(Question_1_Fragment.newInstance(quizAnswers))
        fragments.add(Question_2_Fragment.newInstance(quizAnswers))
        fragments.add(Question_3_Fragment.newInstance(quizAnswers))
        fragments.add(Question_4_Fragment.newInstance(quizAnswers))
        fragments.add(Question_5_Fragment.newInstance(quizAnswers))

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragments[0])
            .commit()

        backButton.setOnClickListener {
            if (currentQuestion > 1) {
                currentQuestion--
                updateFragment(isForward = false)
            }
        }

        nextButton.setOnClickListener {
            if (currentQuestion < totalQuestions) {
                currentQuestion++
                updateFragment(isForward = true)
            } else {
                addUserInformation(email, quizAnswers)
            }
        }

        updateNavigationButtons()
        updateNextButtonState()
    }

    private fun updateNavigationButtons() {
        backButton.isEnabled = currentQuestion > 1
        nextButton.text = if (currentQuestion == totalQuestions) "Завершить" else "Далее"
    }

    private fun updateFragment(isForward: Boolean) {
        val enterAnim = if (isForward) R.anim.slide_in_right else R.anim.slide_in_left
        val exitAnim = if (isForward) R.anim.slide_out_left else R.anim.slide_out_right

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enterAnim, exitAnim)
            .replace(R.id.fragmentContainer, fragments[currentQuestion - 1])
            .commit()

        progressBar.progress = currentQuestion
        updateNavigationButtons()

        if (currentQuestion == 3 || currentQuestion == 4 || currentQuestion == 5) {
            nextButton.isEnabled = true
        } else {
            updateNextButtonState()
        }
    }

    override fun onAnswerSelected() {
        updateNextButtonState()
    }

    private fun updateNextButtonState() {
        nextButton.isEnabled = isCurrentQuestionAnswered()
    }

    private fun isCurrentQuestionAnswered(): Boolean {
        return when (currentQuestion) {
            1 -> quizAnswers.answer1 != null
            2 -> quizAnswers.answer2 != null
            3 -> quizAnswers.answer3 != null
            4 -> quizAnswers.answer4 != null
            5 -> quizAnswers.answer5 != null
            else -> false
        }
    }

    private fun addUserInformation(email: String, quizAnswers: QuizAnswers) {
        val accessToken = sharedPreferences.getString("access_jwt", null) ?: return

        val urlAddFullInformationUser = getString(R.string.url_auth) + "addFullInformationUser"
        val urlAddUserFirstWeight = getString(R.string.url_progress) + "updateUserCurrentWeight"

        val phisTrain = quizAnswers.answer1.toString()
        val targetPhis = quizAnswers.answer2.toString()
        val height = quizAnswers.answer3.toString()
        val weight = quizAnswers.answer4.toString()
        val targetWeight = quizAnswers.answer5.toString()

        // Запрос на добавление полной информации о пользователе
        val formBodyAddFullInformationUser = FormBody.Builder()
            .add("access_token", accessToken)
            .add("phis_train", phisTrain)
            .add("height", height)
            .add("weight", weight)
            .add("target_phis", targetPhis)
            .add("target_weight", targetWeight)
            .build()

        val requestAddFullInformationUser = Request.Builder()
            .url(urlAddFullInformationUser)
            .post(formBodyAddFullInformationUser)
            .build()

        // Запрос на добавление первого веса пользователя
        val formBodyAddUserFirstWeight = FormBody.Builder()
            .add("access_token", accessToken)
            .add("weight", weight)
            .build()

        val requestAddUserFirstWeight = Request.Builder()
            .url(urlAddUserFirstWeight)
            .post(formBodyAddUserFirstWeight)
            .build()

        // Выполнение запросов
        executeRequest(requestAddFullInformationUser)
        executeRequest(requestAddUserFirstWeight)
    }

    private fun executeRequest(request: Request) {
        okhttpclient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logAndShowToast("Ошибка входа", e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseData = response.body?.string()
                    handleResponse(responseData)
                }
            }

            private fun handleResponse(responseData: String?) {
                runOnUiThread {
                    if (responseData != null) {
                        try {
                            val jsonResponse = JSONObject(responseData)
                            when (jsonResponse.getString("answer")) {
                                "Success" -> {
                                    showSuccessToast("Ваши ответы сохранены!")
                                    navigateToMainMenu()
                                }
                                "Error" -> {
                                    Toast.makeText(this@OnboardingQuizActivity, "Ошибка", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    handleErrorResponse(responseData)
                                }
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(this@OnboardingQuizActivity, "Ошибка обработки ответа от сервера", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            private fun handleErrorResponse(responseData: String) {
                val errorMessage = when {
                    responseData.contains("Bad error") -> "Неизвестная ошибка"
                    else -> "Ошибка: $responseData"
                }
                Toast.makeText(this@OnboardingQuizActivity, errorMessage, Toast.LENGTH_SHORT).show()
            }

            private fun logAndShowToast(message: String, throwable: Throwable? = null) {
                Log.e("OnboardingQuizActivity", message, throwable)
                runOnUiThread {
                    Toast.makeText(this@OnboardingQuizActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }



    private fun showSuccessToast(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.layout_success_toast, null)

        val toastText = layout.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        Toast(this@OnboardingQuizActivity).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL, 0, 120)
            show()
        }
    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainHomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }

}
