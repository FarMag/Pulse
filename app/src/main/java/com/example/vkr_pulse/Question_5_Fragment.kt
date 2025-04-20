package com.example.vkr_pulse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import com.example.vkr_pulse.data.QuizAnswers

class Question_5_Fragment : Fragment() {
    private lateinit var quizAnswers: QuizAnswers
    private var listener: OnAnswerSelectedListener? = null
    private lateinit var weightPicker: NumberPicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question_5, container, false)
        weightPicker = view.findViewById(R.id.weightPicker)

        // Настройка диапазона веса
        weightPicker.minValue = 40
        weightPicker.maxValue = 150
        weightPicker.wrapSelectorWheel = false

        if (!::quizAnswers.isInitialized || run {
                val answer5Value = quizAnswers.answer5
                answer5Value == null || answer5Value.isEmpty()
            }) {
            quizAnswers.answer5 = "${weightPicker.value}" // Устанавливаем значение по умолчанию
        }

        // Установка начального значения из предыдущего ответа
        val previousWeight = quizAnswers.answer4?.replace(" кг", "")?.toIntOrNull() ?: 40
        weightPicker.value = previousWeight.coerceIn(weightPicker.minValue, weightPicker.maxValue)

        // Кастомизация шрифта
        try {
            val method = weightPicker.javaClass.getDeclaredMethod("updateView", Int::class.java)
            method.isAccessible = true
            method.invoke(weightPicker, weightPicker.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Обработчик изменений
        weightPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            quizAnswers.answer5 = "$newVal"
            animatePickerChange()
            listener?.onAnswerSelected()
        }

        return view
    }

    private fun animatePickerChange() {
        val scaleAnim = ScaleAnimation(
            1f, 1.1f,
            1f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 150
            repeatCount = 1
            repeatMode = Animation.REVERSE
        }
        weightPicker.startAnimation(scaleAnim)
    }

    // Остальной код без изменений
    companion object {
        fun newInstance(quizAnswers: QuizAnswers): Question_5_Fragment {
            val fragment = Question_5_Fragment()
            fragment.quizAnswers = quizAnswers
            return fragment
        }
    }
}
