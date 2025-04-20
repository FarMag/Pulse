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

class Question_2_Fragment : Fragment() {

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
//        val view = inflater.inflate(R.layout.fragment_question_2, container, false)
//        val answerRadioGroup = view.findViewById<RadioGroup>(R.id.answerRadioGroup)
//
//        answerRadioGroup.setOnCheckedChangeListener { group, checkedId ->
//            val radioButton = group.findViewById<RadioButton>(checkedId)
//            quizAnswers.answer2 = radioButton.text.toString()
//            listener?.onAnswerSelected()
//        }
//
//        return view
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question_2, container, false)
        val answerRadioGroup = view.findViewById<RadioGroup>(R.id.answerRadioGroup)

        answerRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            val answer = when (checkedId) {
                R.id.radioButton1 -> "losing"
                R.id.radioButton2 -> "mass"
                R.id.radioButton3 -> "keeping"
                R.id.radioButton4 -> "longevity"
                else -> null
            }

            answer?.let {
                quizAnswers.answer2 = it
                listener?.onAnswerSelected()
            }
        }

        return view
    }

    companion object {
        fun newInstance(quizAnswers: QuizAnswers): Question_2_Fragment {
            val fragment = Question_2_Fragment()
            fragment.quizAnswers = quizAnswers
            return fragment
        }
    }
}
