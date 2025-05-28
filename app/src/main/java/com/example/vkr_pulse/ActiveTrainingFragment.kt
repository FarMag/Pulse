package com.example.vkr_pulse

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController


class ActiveTrainingFragment : Fragment() {

    private var isTimerRunning = false
    private var elapsedMillis = 0L
    private var lastStartTime = 0L
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_active_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val planId = arguments?.getInt("planId")
        val planName = arguments?.getString("planName")
        val planDesc = arguments?.getString("planDesc")
        val planExercises = arguments?.getString("planExercises")
        val planImageRes = arguments?.getInt("planImageRes")

        view.findViewById<Button>(R.id.notesBtn).setOnClickListener {
            openGlobalNotesDialog()
        }

        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            findNavController().popBackStack() // закрывает текущий экран (фрагмент)
        }
        view.findViewById<Button>(R.id.finishTrainingBtn).setOnClickListener {
            // тут можешь добавить дополнительную логику, если нужно
            findNavController().popBackStack()
        }

        // Теперь наполняем view:
        view.findViewById<TextView>(R.id.planTitle).text = planName
        view.findViewById<TextView>(R.id.planDesc).text = "${planDesc}\n\n${planExercises}"
        planImageRes?.let {
            view.findViewById<ImageView>(R.id.planImage).setImageResource(it)
        }

        val timerText = view.findViewById<TextView>(R.id.timerText)
        val timerStartBtn = view.findViewById<Button>(R.id.timerStartBtn)
        val timerResetBtn = view.findViewById<Button>(R.id.timerResetBtn)

        timerStartBtn.setOnClickListener {
            if (!isTimerRunning) {
                // Стартуем/продолжаем
                isTimerRunning = true
                lastStartTime = System.currentTimeMillis()
                handler.post(updateTimerRunnable)
                timerStartBtn.text = "Пауза"
            } else {
                // Пауза
                isTimerRunning = false
                elapsedMillis += System.currentTimeMillis() - lastStartTime
                handler.removeCallbacks(updateTimerRunnable)
                timerStartBtn.text = "Старт"
            }
        }

        timerResetBtn.setOnClickListener {
            isTimerRunning = false
            elapsedMillis = 0L
            handler.removeCallbacks(updateTimerRunnable)
            timerText.text = "00:00:00"
            timerStartBtn.text = "Старт"
        }
    }

    private fun openGlobalNotesDialog() {
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        // Для главного блокнота ключ, например, "main_notes" или "notes"
        val noteText = preferences.getString("notes", "") ?: ""
        showNotesDialog(noteText)
    }

    private fun showNotesDialog(initialText: String) {
        NotesDialogFragment(initialText) { noteText ->
            saveGlobalNotes(noteText)
        }.show(childFragmentManager, "NotesDialog")
    }

    private fun saveGlobalNotes(noteText: String) {
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        preferences.edit().putString("notes", noteText).apply()
    }

    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                val now = System.currentTimeMillis()
                val total = elapsedMillis + (now - lastStartTime)
                view?.findViewById<TextView>(R.id.timerText)?.text = formatTime(total)
                handler.postDelayed(this, 100)
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateTimerRunnable)
    }
}

