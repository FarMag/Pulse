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

class Question_3_Fragment : Fragment() {
    private lateinit var quizAnswers: QuizAnswers
    private var listener: OnAnswerSelectedListener? = null
    private lateinit var heightPicker: NumberPicker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question_3, container, false)
        heightPicker = view.findViewById(R.id.heightPicker)

        // Настройка диапазона веса
        heightPicker.minValue = 130
        heightPicker.maxValue = 220
        heightPicker.wrapSelectorWheel = false

        // Кастомизация шрифта
        try {
            val method = heightPicker.javaClass.getDeclaredMethod("updateView", Int::class.java)
            method.isAccessible = true
            method.invoke(heightPicker, heightPicker.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Обработчик изменений
        heightPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            quizAnswers.answer3 = "$newVal"
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
        heightPicker.startAnimation(scaleAnim)
    }

    // Остальной код без изменений
    companion object {
        fun newInstance(quizAnswers: QuizAnswers): Question_3_Fragment {
            val fragment = Question_3_Fragment()
            fragment.quizAnswers = quizAnswers
            return fragment
        }
    }
}
