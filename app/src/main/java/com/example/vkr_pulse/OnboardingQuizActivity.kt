package com.example.vkr_pulse

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.vkr_pulse.data.QuizAnswers
import com.example.vkr_pulse.data.User

class OnboardingQuizActivity : AppCompatActivity(), OnAnswerSelectedListener {

    // Количество вопросов
    private val totalQuestions = 5
    private var currentQuestion = 1

    // Объект для хранения ответов теста
    val quizAnswers = QuizAnswers()

    // Объект для хранения данных регистрации пользователя
    lateinit var user: User

    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var progressBar: ProgressBar

    // Массив фрагментов вопросов
    private val fragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_quiz)

        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = totalQuestions

        // Получение данных регистрации из Intent
        val username = intent.getStringExtra("username") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""
        val birthDate = intent.getStringExtra("birth_date") ?: ""
        val gender = intent.getStringExtra("gender") ?: ""
        user = User(username, email, password, birthDate, gender)

        // Инициализируем фрагменты, передавая один и тот же объект quizAnswers
        fragments.add(Question_1_Fragment.newInstance(quizAnswers))
        fragments.add(Question_2_Fragment.newInstance(quizAnswers))
        fragments.add(Question_3_Fragment.newInstance(quizAnswers))
        fragments.add(Question_4_Fragment.newInstance(quizAnswers))
        fragments.add(Question_5_Fragment.newInstance(quizAnswers))

        // Загружаем первый вопрос
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragments[0])
            .commit()

        backButton.setOnClickListener {
            if (currentQuestion > 1) {
                currentQuestion--
                updateFragment(isForward = false) // Передаем флаг направления
            }
        }

        nextButton.setOnClickListener {
            if (currentQuestion < totalQuestions) {
                currentQuestion++
                updateFragment(isForward = true) // Передаем флаг направления
            } else {
                navigateToMainMenu()
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

        // Принудительно активируем кнопку для вопросов 3 и 4
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
            3, 4, 5 -> true // Всегда возвращаем true для вопросов 3 и 4
            else -> false
        }
    }

    private fun updateFragment() {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.fragmentContainer, fragments[currentQuestion - 1])
            .commit()

        progressBar.progress = currentQuestion
        updateNavigationButtons()
        updateNextButtonState()
    }


// если нужно отображение (toast) проверки данных
//    private fun navigateToMainMenu() {
//        // Создаём AlertDialog для отображения данных
//        AlertDialog.Builder(this)
//            .setTitle("Проверка данных")
//            .setMessage("User: $user\nQuiz Answers: $quizAnswers")
//            .setPositiveButton("OK") { dialog, _ ->
//                dialog.dismiss()
//                // После закрытия диалога переходим на главный экран
//                val intent = Intent(this, MainHomeActivity::class.java)
//                startActivity(intent)
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
//                finish()
//            }
//            .show()
//    }

    private fun navigateToMainMenu() {
        val intent = Intent(this, MainHomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        finish()
    }



}
