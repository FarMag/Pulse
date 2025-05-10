package com.example.vkr_pulse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.NumberPicker
import android.widget.TextView
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
        val goalText = view.findViewById<TextView>(R.id.goalText)
        val skipButton = view.findViewById<TextView>(R.id.skipTargetWeightButton)

        weightPicker.minValue = resources.getInteger(R.integer.min_weight)
        weightPicker.maxValue = resources.getInteger(R.integer.max_weight)
        weightPicker.wrapSelectorWheel = false

        val previousWeight = quizAnswers.answer4?.replace(" кг", "")?.toIntOrNull() ?: 60
        weightPicker.value = previousWeight.coerceIn(weightPicker.minValue, weightPicker.maxValue)

        quizAnswers.answer5 = "${weightPicker.value}"

        updateGoalText(goalText, previousWeight, weightPicker.value)

        weightPicker.setOnValueChangedListener { _, _, newVal ->
            quizAnswers.answer5 = "$newVal"
            updateGoalText(goalText, previousWeight, newVal)
            animatePickerChange()
            listener?.onAnswerSelected()
        }

        skipButton.setOnClickListener {
            quizAnswers.answer5 = null
            goalText.text = "Ваша цель: не указана"
            listener?.onAnswerSelected()
        }

        return view
    }


    private fun updateGoalText(goalText: TextView, current: Int, target: Int) {
        val diff = target - current
        val prefix = when {
            diff > 0 -> "+$diff"
            diff < 0 -> "$diff"
            else -> "0"
        }
        goalText.text = "Ваша цель: $prefix кг"
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

    companion object {
        fun newInstance(quizAnswers: QuizAnswers): Question_5_Fragment {
            val fragment = Question_5_Fragment()
            fragment.quizAnswers = quizAnswers
            return fragment
        }
    }
}
