package com.example.vkr_pulse

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.vkr_pulse.data.QuizAnswers

class Question_1_Fragment : Fragment() {

    private lateinit var quizAnswers: QuizAnswers
    private var listener: OnAnswerSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAnswerSelectedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnAnswerSelectedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    //чтобы ответы не форматировались, надо писать это вот внизу
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_question_1, container, false)
//        val answerRadioGroup = view.findViewById<RadioGroup>(R.id.answerRadioGroup)
//
//        answerRadioGroup.setOnCheckedChangeListener { group, checkedId ->
//            val radioButton = group.findViewById<RadioButton>(checkedId)
//            quizAnswers.answer1 = radioButton.text.toString()
//            listener?.onAnswerSelected()
//        }
//
//        return view
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question_1, container, false)
        val answerRadioGroup = view.findViewById<RadioGroup>(R.id.answerRadioGroup)

        answerRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val answer = when (checkedId) {
                R.id.radioButton1 -> "beginner"
                R.id.radioButton2 -> "medium"
                R.id.radioButton3 -> "athlete"
                else -> null
            }

            answer?.let {
                quizAnswers.answer1 = it
                listener?.onAnswerSelected()
            }
        }

        return view
    }

    companion object {
        fun newInstance(quizAnswers: QuizAnswers): Question_1_Fragment {
            val fragment = Question_1_Fragment()
            fragment.quizAnswers = quizAnswers
            return fragment
        }
    }
}
