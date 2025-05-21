package com.example.vkr_pulse

import android.app.VoiceInteractor
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.roundToInt
import com.github.mikephil.charting.formatter.ValueFormatter
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.text.StringCharacterIterator
import java.util.*
import kotlin.math.abs

class HomeFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var greetingText: TextView
    private lateinit var phraseTextView: TextView
    private lateinit var levelText: TextView
    private lateinit var rankText: TextView
    private lateinit var xpLabel: TextView
    private lateinit var xpProgress: ProgressBar
    private lateinit var weightText: TextView
    private lateinit var goalWeightText: TextView
    private lateinit var progressChart: LineChart
    private lateinit var progressPercentText: TextView
    private lateinit var weightLeftText: TextView
    private lateinit var rankImageView: ImageView
    private lateinit var caloriesText: TextView
    private lateinit var caloriesProgressBar: ProgressBar

    private lateinit var phrases: Array<String>
    private var currentPhraseIndex = 0
    private val handler = Handler()
    private lateinit var phraseRunnable: Runnable

    private var previousLevel: Int = -1
//    private var currentTotalXp: Int = 200
    private var currentTotalXp: Int = 0
    private var targetWeight: Double = 0.0
    private var currentWeight: Float = 0F

    private var noteText: String = ""
    private var currentKkal: Int = 0
    private var goalKkal: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val notesButton = view.findViewById<Button>(R.id.notesButton)



        notesButton.setOnClickListener {
            loadNotesAndShowDialog()
        }

        val mainContent = view.findViewById<ScrollView>(R.id.mainContent)
        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loadingIndicator)

        caloriesText = view.findViewById<TextView>(R.id.caloriesText)
        caloriesProgressBar = view.findViewById<ProgressBar>(R.id.caloriesProgressBar)

        mainContent.visibility = View.GONE
        loadingIndicator.visibility = View.VISIBLE
//        sharedPreferences = requireActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
//        val userId = sharedPreferences.getString("sub", null)
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)

//        val targetPhis = fetchUserData(accessToken.toString())
        fetchUserData(accessToken.toString())

        rankImageView = view.findViewById(R.id.rankImageView)
        greetingText = view.findViewById(R.id.greetingText)
        phraseTextView = view.findViewById(R.id.phraseTextView)
        levelText = view.findViewById(R.id.userLevel)
        rankText = view.findViewById(R.id.userRank)
        xpLabel = view.findViewById(R.id.xpLabel)
        xpProgress = view.findViewById(R.id.xpProgress)
        weightText = view.findViewById(R.id.currentWeightText)
        goalWeightText = view.findViewById(R.id.targetWeightText)
        progressChart = view.findViewById(R.id.progressChart)
        weightLeftText = view.findViewById(R.id.weightLeftText)

//        val userName = "Иван"
//        greetingText.text = "За работу, $userName 💪"

//        val (level, currentXp, maxXp, title) = getLevelInfo(currentTotalXp)
//        updateXpUI(level, currentXp, maxXp, title)

//        loadPhrases()
//        startPhraseRotation()
//        setupProgressChart()

        //шаги и прогрессбар для них
        val stepsDone = 0      // получай из БД или часов
        val stepsGoal = 7500      // пользовательская цель
        val stepsPercentage = (stepsDone.toFloat() / stepsGoal * 100).toInt().coerceAtMost(100)

        val stepsText = view.findViewById<TextView>(R.id.stepsText)
        val stepsProgressBar = view.findViewById<ProgressBar>(R.id.stepsProgressBar)

        stepsText.text = "$stepsDone / $stepsGoal шагов"
        stepsProgressBar.progress = stepsPercentage

//        // Кнопка теста добавления XP (временно, для проверки анимации и звука)
//        val testXpButton = view.findViewById<Button>(R.id.testXpButton)
//        testXpButton.setOnClickListener {
//            currentTotalXp += 50
//
//            val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
//            val accessToken = preferences.getString("access_jwt", null)
//            updateUserXp(accessToken.toString(), currentTotalXp)
//
//            val (level, currentXp, maxXp, title) = getLevelInfo(currentTotalXp)
//            updateXpUI(level, currentXp, maxXp, title)
//        }

        val knowledgeCard = view.findViewById<CardView>(R.id.knowledgeCard)
        knowledgeCard.setOnClickListener {
            // Запускаем анимацию переключения текста
//            changeTextOnClick(targetPhis.toString())
            changeTextOnClick()
        }


        val resetWeightLayout = view.findViewById<LinearLayout>(R.id.resetWeightLayout)

        resetWeightLayout.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_reset_weight_history, null)
            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            confirmButton.setOnClickListener {
                resetWeight(accessToken.toString()) {
                    fetchUserData(accessToken.toString())
                    showCustomToast("История веса очищена")
                    dialog.dismiss()
                }
            }


            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

    }

    private fun loadNotesAndShowDialog() {
        showNotesDialog(noteText)
    }

    private fun showNotesDialog(initialText: String) {
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null) ?: return


        fetchUserData(accessToken)
        NotesDialogFragment(initialText) { noteText ->
            saveNotesToServer(noteText)
        }.show(childFragmentManager, "NotesDialog")
    }

    private fun saveNotesToServer(notes: String) {

        if (notes.length > 1023) {
            showToast("Ошибка: Заметка не должна превышать 1023 символов.")
            return
        }

        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null) ?: return

        noteText = notes

        val client = OkHttpClient()
        val url = getString(R.string.url_auth) + "addUserNote"
        val formBody = FormBody.Builder()
            .add("access_token", accessToken)
            .add("notes", notes)
            .build()
        val request = Request.Builder().url(url).post(formBody).build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) { /* Можно показать Toast о неудаче */ }
            override fun onResponse(call: Call, response: Response) { /* Можно показать Toast об успехе */ }
        })

//        fetchUserData(accessToken)
    }


    private fun fetchUserData(accessToken: String) {
        val urlAddresses = listOf("auth", "progress", "nutrition")

        val client = OkHttpClient()

        urlAddresses.forEach { urlAddress ->
            val url = when (urlAddress) {
                "progress" -> getString(R.string.url_progress) + "userProgress"
                "auth" -> getString(R.string.url_auth) + "getUserData"
                "nutrition" -> getString(R.string.url_nutrition) + "getNutritionData"
                else -> throw IllegalArgumentException("Неизвестный адрес URL: $urlAddress")
            }
//            val url = when (urlAddress) {
//                "auth" -> getString(R.string.url_auth) + "getUserData"
//                "progress" -> getString(R.string.url_progress) + "userProgress"
//                else -> throw IllegalArgumentException("Неизвестный адрес URL: $urlAddress")
//            }

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
                        handleResponse(it, urlAddress)
                    }
                }

                private fun handleResponse(response: Response, urlAddress: String) {
                    if (!response.isSuccessful) {
                        handleErrorResponse(response.code)
                        return
                    }

                    val responseData = response.body?.string()
                    if (responseData != null) {
                        try {
                            val jsonResponse = JSONObject(responseData)
                            when (urlAddress) {
                                "progress" -> parseProgressData(jsonResponse)
                                "auth" -> parseUserData(jsonResponse)
                                "nutrition" -> ParseNutritionData(jsonResponse)
                            }
//                            when (urlAddress) {
//                                "auth" -> parseUserData(jsonResponse)
//                                "progress" -> parseProgressData(jsonResponse)
//                            }
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

    }

    private fun updateUserXp(accessToken: String, currentXp: Int) {
        val url = getString(R.string.url_auth) + "updateUserXp"

        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("access_token", accessToken)
            .add("current_xp", currentXp.toString())
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

    private fun resetWeight(accessToken: String, onSuccess: () -> Unit) {
        val url = getString(R.string.url_progress) + "resetWeight"

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
                    if (!response.isSuccessful) {
                        // ...твой error handler...
                        return
                    }
                    // Только по успеху:
                    requireActivity().runOnUiThread {
                        onSuccess() // тут вызывается fetchUserData!
                    }
                }
            }
        })
    }


    fun showToast(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.layout_error_toast, null)

        val toastText = layout.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        val toast = Toast(requireContext().applicationContext)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 120)
        toast.show()
    }



    private fun parseUserData(jsonData: JSONObject){
//        val jsonObject = JSONObject(jsonData)

        val userName = jsonData.getString("username")
//        val level = jsonObject.getInt("level")
//        val currentXp = jsonObject.getInt("currentXp")
//        val maxXp = jsonObject.getInt("maxXp")
//        val title = jsonObject.getString("title")
        val xp = jsonData.getString("xp")
        currentTotalXp = xp.toInt()
        noteText = jsonData.getString("notes")
        val weight = jsonData.getDouble("weight")
        targetWeight = jsonData.getDouble("target_weight")
        val targetPhis = jsonData.getString("target_phis")
//        val (level, currentXp, maxXp, title) = getLevelInfo(currentTotalXp)
//        updateXpUI(level, currentXp, maxXp, title)



        val gender = jsonData.getString("gender")
        val height = jsonData.getString("height")
        val phis_train = jsonData.getString("phis_train")
        val age = jsonData.getString("age")

        val nutrition = calculateNutrition(
            gender = gender,
            age = age.toInt(),
            weightKg = weight.toFloat(),
            heightCm = height.toFloat(),
            activityLevel = phis_train,
            goal = targetPhis
        )
        goalKkal = nutrition.calories.toInt()

        caloriesText.text = "$currentKkal / $goalKkal ккал"




        // Обновляем UI с полученными данными
        requireActivity().runOnUiThread {
            greetingText.text = "За работу, $userName 💪"
//            updateXpUI(level, currentXp, maxXp, title)
            weightText.text = "Текущий вес: ${String.format("%.0f", weight.toFloat())} кг"
//            weightText.text = weight.toString()
            goalWeightText.text = "Цель: ${String.format("%.0f", targetWeight.toFloat())} кг"

//            goalWeightText.text = targetWeight.toString()

//            val (level, currentXp, maxXp, title) = getLevelInfo(currentXp)

            val startWeight = weight.toFloat()
            currentWeight = weight.toFloat()

            val delta = (targetWeight - startWeight).toFloat()
            val progressDelta = currentWeight - startWeight
            val progressPercent = if (delta != 0f) (progressDelta / delta * 100).coerceIn(0f, 100f) else 0f
            val remaining = kotlin.math.abs(currentWeight - targetWeight)

//            progressPercentText.text = "Прогресс: ${progressPercent.toInt()}%"
            weightLeftText.text = "Осталось: ${"%.0f".format(remaining)} кг"

            val (level, currentXp, maxXp, title) = getLevelInfo(currentTotalXp)
            updateXpUI(level, currentXp, maxXp, title)

            startPhraseRotation(targetPhis)

            view?.findViewById<ScrollView>(R.id.mainContent)?.visibility = View.VISIBLE
            view?.findViewById<ProgressBar>(R.id.loadingIndicator)?.visibility = View.GONE
        }
//        return targetPhis
    }

    private fun parseProgressData(jsonData: JSONObject) {
        val firstWeight = jsonData.getJSONObject("first_data_user")
        val startWeight = firstWeight.getString("weight").toFloat()

        val weightHistory = mutableListOf<ProgressData>()
        val weightHistoryJsonArray = jsonData.getJSONArray("all_progress_logs")

        for (i in 0 until weightHistoryJsonArray.length()) {
            val progressItem = weightHistoryJsonArray.getJSONObject(i)
            val date = progressItem.getString("date")
            val weight = progressItem.getString("weight").toFloat()
            weightHistory.add(ProgressData(date, weight))
        }

        val entries = weightHistory.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.weight)
        }

        val dataSet = LineDataSet(entries, "Вес (кг)").apply {
            color = resources.getColor(R.color.teal_700, null)
            valueTextColor = resources.getColor(R.color.black, null)
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawFilled(true)
            fillAlpha = 100
            fillColor = resources.getColor(R.color.teal_200, null)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        progressChart.data = LineData(dataSet)

        progressChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            setTouchEnabled(false)
            setScaleEnabled(false)
            setExtraBottomOffset(14f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -30f
                granularity = 1f
                textSize = 11f
                valueFormatter = DateAxisFormatter(weightHistory.map { it.date })
            }

            axisLeft.apply {
                setDrawGridLines(true)
                setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            }

            legend.isEnabled = false
            invalidate()
        }

        requireActivity().runOnUiThread {
            val currentWeight = currentWeight
            val delta = abs(targetWeight - startWeight)
            val progressDelta = abs(currentWeight - startWeight)
            val progressPercent = if (delta != 0.0) (progressDelta / delta * 100).coerceIn(0.0, 100.0) else 0.0
//            progressPercentText.text = "Прогресс: ${progressPercent.toInt()}%"

            view?.findViewById<ScrollView>(R.id.mainContent)?.visibility = View.VISIBLE
            view?.findViewById<ProgressBar>(R.id.loadingIndicator)?.visibility = View.GONE
        }
    }

    private fun ParseNutritionData(jsonData: JSONObject) {
        view?.findViewById<ProgressBar>(R.id.profileLoading)?.visibility = View.GONE
        view?.findViewById<ScrollView>(R.id.profileContent)?.visibility = View.VISIBLE

        val calories = jsonData.getString("total_calories").toDouble().toInt()
        currentKkal = calories

        val percentage = if (goalKkal > 0) (currentKkal.toFloat() / goalKkal * 100).toInt().coerceAtMost(100) else 0

        requireActivity().runOnUiThread {
            caloriesProgressBar.progress = percentage
            caloriesText.text = "$currentKkal / $goalKkal ккал"
        }
    }


    data class ProgressData(val date: String, val weight: Float) // Класс для хранения данных о прогрессе


    fun showCustomToast(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.layout_success_toast, null)

        val toastText = layout.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        val toast = Toast(requireContext().applicationContext)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 120)
        toast.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(phraseRunnable)
    }


    private fun startPhraseRotation(phraseType: String) {
        phrases = when (phraseType) {
            "mass" -> resources.getStringArray(R.array.muscle_gain_phrases)
            "losing" -> resources.getStringArray(R.array.fat_loss_phrases)
            "keeping" -> resources.getStringArray(R.array.maintenance_phrases)
            "longevity" -> resources.getStringArray(R.array.longevity_phrases)
            else -> emptyArray()
        }

        currentPhraseIndex = 0
        phraseRunnable = object : Runnable {
            override fun run() {
                phraseTextView.animate()
                    .alpha(0f)
                    .translationY(20f)
                    .setDuration(300)
                    .withEndAction {
                        phraseTextView.text = phrases[currentPhraseIndex]
                        phraseTextView.translationY = 20f
                        phraseTextView.alpha = 0f
                        phraseTextView.animate()
                            .alpha(1f)
                            .translationY(0f)
                            .setDuration(500)
                            .start()
                    }.start()

                currentPhraseIndex = (currentPhraseIndex + 1) % phrases.size
                handler.postDelayed(this, 10000)
            }
        }
        handler.post(phraseRunnable)
    }

    private fun changeTextOnClick() {
        // Сначала скрываем текущий текст с анимацией
        phraseTextView.animate()
            .alpha(0f)
            .translationY(20f)
            .setDuration(300)
            .withEndAction {
                // Изменяем индекс фразы и обновляем текст
                currentPhraseIndex = (currentPhraseIndex + 1) % phrases.size
                phraseTextView.text = phrases[currentPhraseIndex]
                phraseTextView.translationY = 20f // устанавливаем перевод для анимации
                phraseTextView.alpha = 0f // скрываем текст

                // Показываем новый текст с анимацией
                phraseTextView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start()
            }
    }


    private fun getLevelInfo(totalXp: Int): LevelInfo {
        val xpTable = generateXpTable(50, 30)
        var accumulatedXp = 0
        var level = 1

        for (i in xpTable.indices) {
            if (totalXp < accumulatedXp + xpTable[i]) {
                val currentXp = totalXp - accumulatedXp
                val maxXp = xpTable[i]
                val title = getRankByLevel(level)
                return LevelInfo(level, currentXp, maxXp, title)
            }
            accumulatedXp += xpTable[i]
            level++
        }
        val title = getRankByLevel(level)
        return LevelInfo(level, totalXp - accumulatedXp, 500, title)
    }

    private fun generateXpTable(startXp: Int, levels: Int): List<Int> {
        val xpList = mutableListOf<Int>()
        for (i in 0 until levels) {
            xpList.add(startXp + i * 10)
        }
        return xpList
    }

    private fun getRankImageResource(rank: String): Int {
        return when (rank) {
            "Новичок" -> R.drawable.cropped_image_1
            "Любитель" -> R.drawable.cropped_image_2
            "Активист" -> R.drawable.cropped_image_3
            "Регуляр" -> R.drawable.cropped_image_4
            "Продвинутый" -> R.drawable.cropped_image_5
            "Атлет" -> R.drawable.cropped_image_6
            "Тренер" -> R.drawable.cropped_image_7
            "Мастер" -> R.drawable.cropped_image_8
            "Грандмастер" -> R.drawable.cropped_image_9
            "Гуру" -> R.drawable.cropped_image_10
            "Легенда" -> R.drawable.cropped_image_11
            "Чемпион" -> R.drawable.cropped_image_12
            else -> R.drawable.cropped_image_1
        }
    }

    private fun updateXpUI(level: Int, currentXp: Int, maxXp: Int, title: String) {
        levelText.text = "Уровень $level"
        rankText.text = "Звание - $title"
        xpLabel.text = "$currentXp XP / $maxXp XP"
        xpProgress.max = maxXp
        animateXpProgress(currentXp)

        // Устанавливаем картинку по званию
        rankImageView.setImageResource(getRankImageResource(title))

        if (previousLevel != -1 && level > previousLevel) {
            showLevelUpToast("Вау! Новый уровень: $level")
        }
        previousLevel = level
    }

    private fun animateXpProgress(xp: Int) {
        xpProgress.progress = 0
        xpProgress.animate().setDuration(500).withStartAction {
            xpProgress.progress = xp
        }.start()
    }

//    class DateAxisFormatter(private val dates: List<String>) : ValueFormatter() {
//        private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        private val outputFormat = SimpleDateFormat("d MMM", Locale("ru"))
//
//        override fun getFormattedValue(value: Float): String {
//            val index = value.toInt()
//            return if (index in dates.indices) {
//                val date = inputFormat.parse(dates[index])
//                outputFormat.format(date ?: Date())
//            } else ""
//        }
//    }

    class DateAxisFormatter(private val dates: List<String>) : ValueFormatter() {
        // Устанавливаем новый формат для входящих строк дат
        private val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        // Оставляем формат для вывода
        private val outputFormat = SimpleDateFormat("d MMM", Locale("ru"))

        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index in dates.indices) {
                try {
                    // Парсим дата-строку с учетом нового формата
                    val date = inputFormat.parse(dates[index])
                    // Форматируем дату в нужный вывод
                    outputFormat.format(date ?: Date())
                } catch (e: ParseException) {
                    e.printStackTrace() // Логирование ошибки
                    "" // Возвращаем пустую строку в случае ошибки
                }
            } else ""
        }
    }


    private fun getRankByLevel(level: Int): String {
        return when (level) {
            in 1..4 -> "Новичок"
            in 5..9 -> "Любитель"
            in 10..14 -> "Активист"
            in 15..19 -> "Регуляр"
            in 20..24 -> "Продвинутый"
            in 25..29 -> "Атлет"
            in 30..34 -> "Тренер"
            in 35..39 -> "Мастер"
            in 40..44 -> "Грандмастер"
            in 45..49 -> "Гуру"
            in 50..54 -> "Легенда"
            else -> "Чемпион"
        }
    }


    private fun showLevelUpToast(message: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.layout_new_level_toast, null)
        val toastText = layout.findViewById<TextView>(R.id.toastText)
        toastText.text = message

        val icon = layout.findViewById<ImageView>(R.id.toastIcon)
        icon.setImageResource(R.drawable.ic_star_celebration)

        val toast = Toast(requireContext().applicationContext)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 120)
        toast.show()

        MediaPlayer.create(requireContext(), R.raw.level_up_ding_2).start()
    }

//    private fun setupProgressChart() {
//        // Пример данных (в будущем ты получишь их с сервера)
//        val weightHistory = listOf(
//            WeightEntry("2025-05-01", 78f),
//            WeightEntry("2025-05-03", 75.4f),
//            WeightEntry("2025-05-05", 74.8f)
//
//        )
//
//        val entries = weightHistory.mapIndexed { index, entry ->
//            Entry(index.toFloat(), entry.weight)
//        }
//
//        val dataSet = LineDataSet(entries, "Вес (кг)").apply {
//            color = resources.getColor(R.color.teal_700, null)
//            valueTextColor = resources.getColor(R.color.black, null)
//            lineWidth = 2.5f
//            circleRadius = 4f
//            setDrawFilled(true)
//            fillAlpha = 100
//            fillColor = resources.getColor(R.color.teal_200, null)
//            mode = LineDataSet.Mode.CUBIC_BEZIER
//        }
//
//        dataSet.setDrawValues(false) // отключить отображение значений на графике
//        progressChart.data = LineData(dataSet)
//
//        progressChart.apply {
//            description.isEnabled = false
//            axisRight.isEnabled = false
//
//            xAxis.position = XAxis.XAxisPosition.BOTTOM
//            xAxis.setDrawGridLines(false)
//            xAxis.labelRotationAngle = -30f
//            xAxis.granularity = 1f
//            xAxis.valueFormatter = DateAxisFormatter(weightHistory.map { it.date })
//
//            axisLeft.setDrawGridLines(true)
//            axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
//
//            legend.isEnabled = false
//            setTouchEnabled(false)
//            invalidate()
//        }
//
//        val startWeight = weightHistory.first().weight
//        val currentWeight = weightHistory.last().weight
//        val goalWeight = 70f
//
//        weightText.text = "Текущий вес: ${currentWeight.toInt()} кг"
//        goalWeightText.text = "Цель: ${goalWeight.toInt()} кг"
//
//        val delta = goalWeight - startWeight
//        val progressDelta = currentWeight - startWeight
//        val progressPercent = if (delta != 0f) (progressDelta / delta * 100).coerceIn(0f, 100f) else 0f
//        val remaining = kotlin.math.abs(currentWeight - goalWeight)
//
////        progressPercentText.text = "Прогресс: ${progressPercent.toInt()} хахахаха%"
//        weightLeftText.text = "Осталось: ${"%.1f".format(remaining)} кг"
//    }

    private fun calculateNutrition(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String
    ): NutritionResult {
        // BMR по формуле Mifflin-St Jeor
        val bmr = if (gender.lowercase() == "male") {
            10 * weightKg + 6.25f * heightCm - 5 * age + 5
        } else {
            10 * weightKg + 6.25f * heightCm - 5 * age - 161
        }

        // Активность
        val activityMultiplier = when (activityLevel) {
            "beginner" -> 1.375f
            "medium" -> 1.55f
            "athlete" -> 1.725f
            else -> 1.375f
        }
        val tdee = bmr * activityMultiplier

        // Цель
        val calories = when (goal) {
            "losing" -> tdee * 0.80f
            "mass" -> tdee * 1.15f
            "keeping", "longevity" -> tdee
            else -> tdee
        }

        // Возвращаем объект NutritionResult с рассчитанными калориями
        return NutritionResult(calories)
    }


    data class NutritionResult(val calories: Float)

    data class LevelInfo(
        val level: Int,
        val currentXp: Int,
        val maxXp: Int,
        val title: String
    )

    data class WeightEntry(
        val date: String,  // формат: "2025-05-01"
        val weight: Float
    )
}
