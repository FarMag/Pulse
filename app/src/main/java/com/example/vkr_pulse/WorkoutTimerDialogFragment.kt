package com.example.vkr_pulse

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class WorkoutTimerDialogFragment : DialogFragment() {

    private var isStopwatchMode = true
    private var isRunning = false
    private var stopwatchElapsed = 0L
    private var stopwatchStartTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerTextView: TextView
    private var timer: CountDownTimer? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_workout_timer, null)

        timerTextView = view.findViewById(R.id.timerTextView)
        val inputTimerEditText = view.findViewById<EditText>(R.id.inputTimerEditText)
        val stopwatchModeButton = view.findViewById<Button>(R.id.stopwatchModeButton)
        val timerModeButton = view.findViewById<Button>(R.id.timerModeButton)
        val startPauseButton = view.findViewById<ImageButton>(R.id.startPauseButton)
        val resetButton = view.findViewById<ImageButton>(R.id.resetButton)
        val closeButton = view.findViewById<Button>(R.id.closeTimerDialogButton)

        // Текущий статус для кнопки play/pause
        var isPauseState = false

        fun switchMode(toStopwatch: Boolean) {
            isStopwatchMode = toStopwatch
            timerTextView.text = "00:00:00"
            isRunning = false
            stopwatchElapsed = 0L
            handler.removeCallbacks(updateRunnable)
            timer?.cancel()
            inputTimerEditText.visibility = if (isStopwatchMode) View.GONE else View.VISIBLE

            // Восстанавливаем иконку play для start/pause
            startPauseButton.setImageResource(R.drawable.ic_play)
            isPauseState = false

            stopwatchModeButton.setTextColor(
                if (toStopwatch) resources.getColor(R.color.pink) else resources.getColor(R.color.blue)
            )
            timerModeButton.setTextColor(
                if (!toStopwatch) resources.getColor(R.color.pink) else resources.getColor(R.color.blue)
            )
        }

        stopwatchModeButton.setOnClickListener { switchMode(true) }
        timerModeButton.setOnClickListener { switchMode(false) }

        startPauseButton.setOnClickListener {
            if (isStopwatchMode) {
                if (!isRunning) {
                    // Start
                    isRunning = true
                    stopwatchStartTime = System.currentTimeMillis()
                    handler.post(updateRunnable)
                    startPauseButton.setImageResource(R.drawable.ic_pause)
                    isPauseState = true
                } else {
                    // Pause
                    isRunning = false
                    stopwatchElapsed += System.currentTimeMillis() - stopwatchStartTime
                    handler.removeCallbacks(updateRunnable)
                    startPauseButton.setImageResource(R.drawable.ic_play)
                    isPauseState = false
                }
            } else {
                // Таймер
                if (!isRunning) {
                    val minutesInput = inputTimerEditText.text.toString().toLongOrNull() ?: 0L
                    val millisInFuture = minutesInput * 60 * 1000
                    if (millisInFuture <= 0) {
                        timerTextView.text = "00:00:00"
                        return@setOnClickListener
                    }
                    timer = object : CountDownTimer(millisInFuture, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            timerTextView.text = formatTime(millisUntilFinished)
                        }
                        override fun onFinish() {
                            timerTextView.text = "00:00:00"
                            isRunning = false
                            startPauseButton.setImageResource(R.drawable.ic_play)
                            isPauseState = false
                        }
                    }.start()
                    isRunning = true
                    startPauseButton.setImageResource(R.drawable.ic_pause)
                    isPauseState = true
                } else {
                    timer?.cancel()
                    isRunning = false
                    startPauseButton.setImageResource(R.drawable.ic_play)
                    isPauseState = false
                }
            }
        }

        resetButton.setOnClickListener {
            if (isStopwatchMode) {
                isRunning = false
                stopwatchElapsed = 0L
                handler.removeCallbacks(updateRunnable)
                timerTextView.text = "00:00:00"
                startPauseButton.setImageResource(R.drawable.ic_play)
                isPauseState = false
            } else {
                timer?.cancel()
                isRunning = false
                timerTextView.text = "00:00:00"
                startPauseButton.setImageResource(R.drawable.ic_play)
                isPauseState = false
            }
        }

        closeButton.setOnClickListener { dismiss() }

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        return dialog
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                val now = System.currentTimeMillis()
                val total = stopwatchElapsed + (now - stopwatchStartTime)
                timerTextView.text = formatTime(total)
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
}
