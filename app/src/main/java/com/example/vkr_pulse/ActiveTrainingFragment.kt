//package com.example.vkr_pulse
//
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//

//class ActiveTrainingFragment : Fragment() {
//
//    private var isTimerRunning = false
//    private var elapsedMillis = 0L
//    private var lastStartTime = 0L
//    private val handler = Handler(Looper.getMainLooper())
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_active_training, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val planId = arguments?.getInt("planId")
//        val planName = arguments?.getString("planName")
//        val planDesc = arguments?.getString("planDesc")
//        val planExercises = arguments?.getString("planExercises")
//        val planImageRes = arguments?.getInt("planImageRes")
//
//        view.findViewById<Button>(R.id.notesBtn).setOnClickListener {
//            openGlobalNotesDialog()
//        }
//
//        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
//            findNavController().popBackStack() // закрывает текущий экран (фрагмент)
//        }
//        view.findViewById<Button>(R.id.finishTrainingBtn).setOnClickListener {
//            // тут можешь добавить дополнительную логику, если нужно
//            findNavController().popBackStack()
//        }
//
//        // Теперь наполняем view:
//        view.findViewById<TextView>(R.id.planTitle).text = planName
//        view.findViewById<TextView>(R.id.planDesc).text = "${planDesc}\n\n${planExercises}"
//        planImageRes?.let {
//            view.findViewById<ImageView>(R.id.planImage).setImageResource(it)
//        }
//
//        val timerText = view.findViewById<TextView>(R.id.timerText)
//        val timerStartBtn = view.findViewById<Button>(R.id.timerStartBtn)
//        val timerResetBtn = view.findViewById<Button>(R.id.timerResetBtn)
//
//        timerStartBtn.setOnClickListener {
//            if (!isTimerRunning) {
//                // Стартуем/продолжаем
//                isTimerRunning = true
//                lastStartTime = System.currentTimeMillis()
//                handler.post(updateTimerRunnable)
//                timerStartBtn.text = "Пауза"
//            } else {
//                // Пауза
//                isTimerRunning = false
//                elapsedMillis += System.currentTimeMillis() - lastStartTime
//                handler.removeCallbacks(updateTimerRunnable)
//                timerStartBtn.text = "Старт"
//            }
//        }
//
//        timerResetBtn.setOnClickListener {
//            isTimerRunning = false
//            elapsedMillis = 0L
//            handler.removeCallbacks(updateTimerRunnable)
//            timerText.text = "00:00:00"
//            timerStartBtn.text = "Старт"
//        }
//    }
//
//    private fun openGlobalNotesDialog() {
//        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
//        // Для главного блокнота ключ, например, "main_notes" или "notes"
//        val noteText = preferences.getString("notes", "") ?: ""
//        showNotesDialog(noteText)
//    }
//
//    private fun showNotesDialog(initialText: String) {
//        NotesDialogFragment(initialText) { noteText ->
//            saveGlobalNotes(noteText)
//        }.show(childFragmentManager, "NotesDialog")
//    }
//
//    private fun saveGlobalNotes(noteText: String) {
//        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
//        preferences.edit().putString("notes", noteText).apply()
//    }
//
//    private val updateTimerRunnable = object : Runnable {
//        override fun run() {
//            if (isTimerRunning) {
//                val now = System.currentTimeMillis()
//                val total = elapsedMillis + (now - lastStartTime)
//                view?.findViewById<TextView>(R.id.timerText)?.text = formatTime(total)
//                handler.postDelayed(this, 100)
//            }
//        }
//    }
//
//    private fun formatTime(millis: Long): String {
//        val totalSeconds = millis / 1000
//        val hours = totalSeconds / 3600
//        val minutes = (totalSeconds % 3600) / 60
//        val seconds = totalSeconds % 60
//        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        handler.removeCallbacks(updateTimerRunnable)
//    }
//}
//


































package com.example.vkr_pulse

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class ActiveTrainingFragment : Fragment() {

    private var isTimerRunning = false
    private var elapsedMillis = 0L
    private var lastStartTime = 0L

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
            findNavController().popBackStack()
        }

        val timerText = view.findViewById<TextView>(R.id.timerText)
        val timerStartBtn = view.findViewById<Button>(R.id.timerStartBtn)
        val timerResetBtn = view.findViewById<Button>(R.id.timerResetBtn)

        timerStartBtn.setOnClickListener {
            if (!isTimerRunning) {
                isTimerRunning = true
                lastStartTime = System.currentTimeMillis()
                startTimer()
                timerStartBtn.text = "Пауза"
            } else {
                isTimerRunning = false
                elapsedMillis += System.currentTimeMillis() - lastStartTime
                stopTimer()
                timerStartBtn.text = "Старт"
            }
        }

        timerResetBtn.setOnClickListener {
            isTimerRunning = false
            elapsedMillis = 0L
            stopTimer()
            timerText.text = "00:00:00"
            timerStartBtn.text = "Старт"
        }

        // Кнопка "Завершить тренировку"
        view.findViewById<Button>(R.id.finishTrainingBtn).setOnClickListener {
            val finalTime = timerText.text.toString()

            val preferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
            val accessToken = preferences.getString("access_jwt", null)

            // Передать данные на сервер
            sendTrainingData(finalTime, planId)
            updateUserXp(accessToken.toString())
            findNavController().popBackStack()
        }

        // Теперь наполняем view:
        view.findViewById<TextView>(R.id.planTitle).text = planName
        view.findViewById<TextView>(R.id.planDesc).text = "${planDesc}\n\n${planExercises}"
        planImageRes?.let {
            view.findViewById<ImageView>(R.id.planImage).setImageResource(it)
        }
    }

    private fun startTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            while (isTimerRunning) {
                val now = System.currentTimeMillis()
                val total = elapsedMillis + (now - lastStartTime)
                view?.findViewById<TextView>(R.id.timerText)?.text = formatTime(total)
                delay(100)
            }
        }
    }

    private fun stopTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            isTimerRunning = false
            delay(100)
        }
    }

    private fun sendTrainingData(timerText: String, planId: Int?) {
        // Получаем access_token из SharedPreferences
        val preferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null) ?: return

        val url = getString(R.string.url_workouts) + "addTodayUserTraining" // Добавь в strings.xml строку url_add_today_user_training

        // Собираем тело запроса
        val formBody = FormBody.Builder()
            .add("timer", timerText)
            .add("planId", planId?.toString() ?: "")
            .add("access_token", accessToken)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Можешь добавить обработку ошибок через Toast или Log
            }

            override fun onResponse(call: Call, response: Response) {
                // Можешь добавить обработку успешного ответа, если нужно
                response.close()
            }
        })
    }

    // Остальной код (notes и таймер) без изменений...
//    private fun openGlobalNotesDialog() {
//        val preferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
//        val noteText = preferences.getString("notes", "") ?: ""
//        showNotesDialog(noteText)
//    }
    private fun openGlobalNotesDialog() {
        val preferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null) ?: return
        fetchUserNoteFromServer(accessToken)
    }

    private fun fetchUserNoteFromServer(accessToken: String) {
        val client = OkHttpClient()
        val url = getString(R.string.url_auth) + "getUserData"
        val formBody = FormBody.Builder()
            .add("access_token", accessToken)
            .build()
        val request = Request.Builder().url(url).post(formBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showToast("Ошибка загрузки заметки")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseData = it.body?.string()
                    requireActivity().runOnUiThread {
                        try {
                            val json = JSONObject(responseData)
                            val notes = json.optString("notes", "")
                            showNotesDialog(notes)
                        } catch (e: JSONException) {
                            showToast("Ошибка разбора заметки")
                        }
                    }
                }
            }
        })
    }

    private fun updateUserXp(accessToken: String) {
        val url = getString(R.string.url_auth) + "updateUserXp"

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("access_token", accessToken)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                showToast("Ошибка получения данных")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    handleResponse(response)
                }
            }

            private fun handleResponse(response: Response) {
                if (!response.isSuccessful) {
                    handleErrorResponse(response.code)
                    return
                }

                val responseData = response.body?.string()
                if (responseData != null) {
                    try {
                        val jsonResponse = JSONObject(responseData)
//                        parseProgressData(jsonResponse)
                    } catch (e: JSONException) {
                        showToast("Ошибка разбора данных")
                    }
                } else {
                    showToast("Пустой ответ от сервера")
                }
            }

            private fun handleErrorResponse(code: Int) {
                val errorMessage = when (code) {
                    400 -> "Ошибка: ID не предоставлен"
                    404 -> "Ошибка: Пользователь не найден"
                    500 -> "Ошибка сервера, попробуйте позже"
                    else -> "Неизвестная ошибка: $code"
                }
                showToast(errorMessage)
            }
        })
    }

//    private fun showNotesDialog(initialText: String) {
//        NotesDialogFragment(initialText) { noteText ->
//            saveGlobalNotes(noteText)
//        }.show(childFragmentManager, "NotesDialog")
//    }

    private fun showNotesDialog(initialText: String) {
        NotesDialogFragment(initialText) { noteText ->
            saveNotesToServer(noteText)
        }.show(childFragmentManager, "NotesDialog")
    }

    private fun saveNotesToServer(notes: String) {
        if (notes.length > 1023) {
            showToast("Ошибка: Заметка не должна превышать 1023 символов.")
            return
        }
        val preferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null) ?: return

        val client = OkHttpClient()
        val url = getString(R.string.url_auth) + "addUserNote"
        val formBody = FormBody.Builder()
            .add("access_token", accessToken)
            .add("notes", notes)
            .build()
        val request = Request.Builder().url(url).post(formBody).build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    showToast("Ошибка сохранения заметки")
                }
            }
            override fun onResponse(call: Call, response: Response) {
                requireActivity().runOnUiThread {
                    showToast("Заметка сохранена")
                }
            }
        })
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun saveGlobalNotes(noteText: String) {
        val preferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        preferences.edit().putString("notes", noteText).apply()
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
