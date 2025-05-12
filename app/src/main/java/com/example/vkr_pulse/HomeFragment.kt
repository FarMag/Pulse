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

    private lateinit var phrases: Array<String>
    private var currentPhraseIndex = 0
    private val handler = Handler()
    private lateinit var phraseRunnable: Runnable

    private var previousLevel: Int = -1
//    private var currentTotalXp: Int = 200
    private var currentTotalXp: Int = 0
    private var targetWeight: Double = 0.0
    private var currentWeight: Float = 0F

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val mainContent = view.findViewById<ScrollView>(R.id.mainContent)
        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loadingIndicator)

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

        //отмечаем калории, надо будет брать их бд
        val caloriesConsumed = 1000
        val caloriesTarget = 1800
        val percentage = (caloriesConsumed.toFloat() / caloriesTarget * 100).toInt().coerceAtMost(100)

        val caloriesText = view.findViewById<TextView>(R.id.caloriesText)
        val progressBar = view.findViewById<ProgressBar>(R.id.caloriesProgressBar)
        caloriesText.text = "$caloriesConsumed / $caloriesTarget ккал"
        progressBar.progress = percentage

        //шаги и прогрессбар для них
        val stepsDone = 3400      // получай из БД или часов
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
                //здесь будет очистка истории веса
                resetWeight(accessToken.toString())
                fetchUserData(accessToken.toString())
                dialog.dismiss()
                showCustomToast("История веса очищена")
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

    }




    private fun fetchUserData(accessToken: String) {
        val urlAddresses = listOf("auth", "progress")

        val client = OkHttpClient()

        urlAddresses.forEach { urlAddress ->
            val url = when (urlAddress) {
                "progress" -> getString(R.string.url_progress) + "userProgress"
                "auth" -> getString(R.string.url_auth) + "getUserData"
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

    private fun resetWeight(accessToken: String) {
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



    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }











//    private fun fetchUserData(access_token: String) {
//        val url = getString(R.string.url_auth) + "getUserData"
//
//        val client = OkHttpClient()
////        val request = VoiceInteractor.Request.Builder().url(url).build()
//
//        val formBody = FormBody.Builder()
//            .add("access_token", access_token)
//            .build()
//
//        val request = Request.Builder()
//            .url(url)
//            .post(formBody)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//                requireActivity().runOnUiThread {
//                    Toast.makeText(requireContext(), "Ошибка получения данных", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    // Используем функцию handleResponse для обработки ответа
//                    handleResponse(response)
//                }
//            }
//
//            private fun handleResponse(response: Response) {
//                if (!response.isSuccessful) {
//                    handleErrorResponse(response.code)
//                    return
//                }
//
//                val responseData = response.body?.string()
//                if (responseData != null) {
//                    try {
//                        val jsonResponse = JSONObject(responseData)
//                        parseUserData(jsonResponse)
//                    } catch (e: JSONException) {
//                        Toast.makeText(requireContext(), "Ошибка разбора данных", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    Toast.makeText(requireContext(), "Пустой ответ от сервера", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            private fun handleErrorResponse(code: Int) {
//                requireActivity().runOnUiThread {
//                    when (code) {
//                        400 -> Toast.makeText(requireContext(), "Ошибка: ID не предоставлен", Toast.LENGTH_SHORT).show()
//                        404 -> Toast.makeText(requireContext(), "Ошибка: Пользователь не найден", Toast.LENGTH_SHORT).show()
//                        500 -> Toast.makeText(requireContext(), "Ошибка сервера, попробуйте позже", Toast.LENGTH_SHORT).show()
//                        else -> Toast.makeText(requireContext(), "Неизвестная ошибка: $code", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        })
//    }

//    private fun parseUserData(jsonData: String) {
//        val jsonObject = JSONObject(jsonData)
//        val userName = jsonObject.getString("username")
////        val level = jsonObject.getInt("level")
////        val currentXp = jsonObject.getInt("currentXp")
////        val maxXp = jsonObject.getInt("maxXp")
////        val title = jsonObject.getString("title")
//        val weight = jsonObject.getDouble("weight")
//        val targetWeight = jsonObject.getDouble("target_weight")

    private fun parseUserData(jsonData: JSONObject){
//        val jsonObject = JSONObject(jsonData)

        val userName = jsonData.getString("username")
//        val level = jsonObject.getInt("level")
//        val currentXp = jsonObject.getInt("currentXp")
//        val maxXp = jsonObject.getInt("maxXp")
//        val title = jsonObject.getString("title")
        val xp = jsonData.getString("xp")
        currentTotalXp = xp.toInt()
        val weight = jsonData.getDouble("weight")
        targetWeight = jsonData.getDouble("target_weight")
        val targetPhis = jsonData.getString("target_phis")
//        val (level, currentXp, maxXp, title) = getLevelInfo(currentTotalXp)
//        updateXpUI(level, currentXp, maxXp, title)










//        greetingText.text = "За работу, $userName 💪"
////            updateXpUI(level, currentXp, maxXp, title)
//        weightText.text = "Текущий вес: ${String.format("%.0f", weight.toFloat())} кг"
////            weightText.text = weight.toString()
//        goalWeightText.text = "Цель: ${String.format("%.0f", targetWeight.toFloat())} кг"
//
////            goalWeightText.text = targetWeight.toString()
//
////            val (level, currentXp, maxXp, title) = getLevelInfo(currentXp)
//
//        val startWeight = weight.toFloat()
//        currentWeight = weight.toFloat()
//
//        val delta = (targetWeight - startWeight).toFloat()
//        val progressDelta = currentWeight - startWeight
//        val progressPercent = if (delta != 0f) (progressDelta / delta * 100).coerceIn(0f, 100f) else 0f
//        val remaining = kotlin.math.abs(currentWeight - targetWeight)
//
////            progressPercentText.text = "Прогресс: ${progressPercent.toInt()}%"
//        weightLeftText.text = "Осталось: ${"%.0f".format(remaining)} кг"
//
//        val (level, currentXp, maxXp, title) = getLevelInfo(currentTotalXp)
//        updateXpUI(level, currentXp, maxXp, title)
//
//        startPhraseRotation(targetPhis)









//        currentWeight = weight.toFloat()

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
            setScaleEnabled(false) // 🔒 запрещаем масштаб
            setExtraBottomOffset(14f) // ⬇️ фикс отступ снизу

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                labelRotationAngle = -30f
                granularity = 1f
                textSize = 11f // 🔤 чтобы не обрезалось
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

//    private fun loadPhrases() {
//        phrases = listOf( //временные фразы для проверки - надо их удалить
//            "Регулярные тренировки замедляют процессы старения клеток.",
//            "30 минут активности в день снижают риск инфаркта на 50%.",
//            "Силовые тренировки увеличивают плотность костной ткани.",
//        )
//
//        val muscleGainPhrases = listOf( //набор массы
//            "Гипертрофия мышц запускается при 60–80% от 1ПМ нагрузки.",
//            "Для роста мышц важен прогрессивный перегруз — увеличивай вес или повторения.",
//            "Сон менее 6 часов снижает уровень тестостерона и замедляет рост мышц.",
//            "Белок после тренировки ускоряет мышечное восстановление и рост.",
//            "Для набора массы нужно потреблять больше калорий, чем тратишь.",
//            "Мышцы растут не на тренировке, а во время отдыха.",
//            "Оптимальная частота тренировок — 2–3 раза в неделю на каждую группу мышц.",
//            "Силовые упражнения активируют анаболические гормоны, включая IGF-1 и тестостерон.",
//            "20–30 г белка в один приём пищи — максимум, который эффективно усваивается.",
//            "Тренировки до отказа стимулируют большее мышечное напряжение.",
//            "Комплексные упражнения (присед, жим, тяга) лучше всего запускают рост мышц.",
//            "Креатин моногидрат — самый изученный и эффективный натуральный добавочный стимулятор силы.",
//            "Гормон роста повышается во сне — не пренебрегай качественным отдыхом.",
//            "Омега-3 улучшает чувствительность к инсулину и ускоряет восстановление.",
//            "Недостаток углеводов — причина низкой энергии и замедленного роста мышц.",
//            "Растущая нагрузка — ключ к длительному прогрессу.",
//            "Слишком частые тренировки без восстановления могут привести к катаболизму.",
//            "BCAA помогают защитить мышцы во время интенсивных нагрузок.",
//            "Тестостерон — главный гормон роста мышц, и его уровень можно поддерживать естественно.",
//            "Отказ от алкоголя улучшает синтез белка и повышает силу."
//        )
//
//        val fatLossPhrases = listOf( //похудение
//            "Дефицит калорий — главный фактор снижения веса.",
//            "Сон менее 6 часов нарушает регуляцию гормонов голода — лептина и грелина.",
//            "Силовые тренировки сохраняют мышечную массу во время похудения.",
//            "Углеводы не враги — главное контролировать общее потребление калорий.",
//            "Кардио усиливает сжигание жира, особенно в сочетании с силовыми.",
//            "Высокобелковая диета помогает сохранить мышечную массу при дефиците калорий.",
//            "Нерегулярное питание увеличивает риск переедания.",
//            "Питьевая вода перед приёмами пищи снижает аппетит.",
//            "Хронический стресс повышает уровень кортизола и способствует накоплению жира.",
//            "Еда с высоким гликемическим индексом может усиливать чувство голода.",
//            "Дефицит железа и витамина D может замедлить метаболизм.",
//            "Интервальное голодание может помочь создать дефицит калорий, но не работает без контроля рациона.",
//            "Наблюдение за прогрессом повышает мотивацию и уменьшает вероятность срывов.",
//            "Слишком резкое снижение калорий замедляет обмен веществ.",
//            "Ночные перекусы тормозят жиросжигание и ухудшают сон.",
//            "Потеря веса без физической активности может привести к потере мышц.",
//            "Минимум 7000 шагов в день помогает ускорить сжигание жира.",
//            "Рацион с низким содержанием обработанных продуктов снижает тягу к еде.",
//            "Регулярный приём пищи стабилизирует уровень сахара и снижает риск переедания.",
//            "Потеря 0.5–1 кг в неделю — здоровый и устойчивый темп."
//        )
//
//        val maintenancePhrases = listOf( //поддержание формы
//            "Регулярная активность снижает риск диабета 2 типа на 30–50%.",
//            "Поддержание формы — это стабильность, а не жёсткие диеты и марафоны.",
//            "Достаточная физическая нагрузка помогает регулировать артериальное давление.",
//            "30 минут умеренной активности в день поддерживают здоровье сердца и сосудов.",
//            "Силовые упражнения 2 раза в неделю помогают сохранить мышечную массу с возрастом.",
//            "Правильное питание стабилизирует уровень энергии и настроение.",
//            "Хорошая форма — результат системности, а не идеальности.",
//            "Физическая активность улучшает качество сна и бодрость днём.",
//            "Баланс белков, жиров и углеводов важен даже при поддержании веса.",
//            "Регулярные прогулки снижают уровень тревожности и улучшают настроение.",
//            "Физическая форма напрямую влияет на работоспособность и концентрацию.",
//            "Гибкость и мобильность суставов важны не меньше силы и выносливости.",
//            "Стабильный вес снижает нагрузку на суставы и позвоночник.",
//            "Поддержание формы снижает риск возрастных заболеваний.",
//            "Рацион с достаточным количеством клетчатки улучшает пищеварение и снижает аппетит.",
//            "Даже 15 минут растяжки в день улучшают самочувствие.",
//            "Сохранять форму проще, чем возвращать её после перерыва.",
//            "Физическая активность укрепляет иммунитет и улучшает восстановление после болезней.",
//            "Контроль порций помогает держать вес без строгих ограничений.",
//            "Регулярный режим сна и бодрствования способствует стабильному обмену веществ."
//        )
//
//        val longevityPhrases = listOf( //спортивное долголение
//            "Силовые тренировки после 50 помогают сохранить плотность костей и предотвратить остеопороз.",
//            "Регулярные упражнения снижают риск болезней сердца на 40–50%.",
//            "Физическая активность уменьшает возрастное снижение когнитивных функций.",
//            "Даже лёгкие тренировки 3 раза в неделю улучшают баланс и координацию.",
//            "Потеря мышечной массы начинается после 40 лет — движение сохраняет силу.",
//            "Силовые и аэробные тренировки помогают контролировать уровень сахара и инсулин.",
//            "Упражнения с собственным весом защищают суставы и развивают выносливость.",
//            "Физическая активность снижает риск падений на 30% у людей старше 65 лет.",
//            "Поддержание массы мышц связано с более долгой и здоровой жизнью.",
//            "Даже 20 минут ходьбы в день продлевают жизнь и снижают воспалительные процессы.",
//            "Растяжка и дыхательные упражнения улучшают подвижность и насыщение тканей кислородом.",
//            "Сон, питание и движение — три столпа долголетия.",
//            "Умеренная активность повышает уровень «хорошего» холестерина (ЛПВП).",
//            "Сильные мышцы облегчают повседневную жизнь — от подъёма с кресла до прогулки.",
//            "Здоровая активность снижает риск деменции на 30–40%.",
//            "Возраст — не преграда: тело адаптируется к тренировкам в любом возрасте.",
//            "Физически активные люди старше 60 реже страдают депрессией и тревожностью.",
//            "Медленное старение — результат регулярных тренировок, а не генетики.",
//            "Поддержание физической формы помогает сохранить независимость в пожилом возрасте.",
//            "Тренировки улучшают работу митохондрий — источников энергии в каждой клетке."
//        )
//
//    }

//    private fun startPhraseRotation(phraseType: String) {
//        phraseRunnable = object : Runnable {
//            override fun run() {
//                phraseTextView.animate()
//                    .alpha(0f)
//                    .translationY(20f)
//                    .setDuration(300)
//                    .withEndAction {
//                        phraseTextView.text = phrases[currentPhraseIndex]
//                        phraseTextView.translationY = 20f
//                        phraseTextView.alpha = 0f
//                        phraseTextView.animate()
//                            .alpha(1f)
//                            .translationY(0f)
//                            .setDuration(500)
//                            .start()
//                    }.start()
//
//                currentPhraseIndex = (currentPhraseIndex + 1) % phrases.size
//                handler.postDelayed(this, 10000)
//            }
//        }
//        handler.post(phraseRunnable)
//    }

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

    private fun setupProgressChart() {
        // Пример данных (в будущем ты получишь их с сервера)
        val weightHistory = listOf(
            WeightEntry("2025-05-01", 78f),
            WeightEntry("2025-05-03", 75.4f),
            WeightEntry("2025-05-05", 74.8f),
            WeightEntry("2025-05-07", 74.2f),
            WeightEntry("2025-05-09", 73.9f),
            WeightEntry("2025-05-11", 73.2f),
            WeightEntry("2025-05-13", 72.8f),
            WeightEntry("2025-05-14", 76.4f),
            WeightEntry("2025-05-18", 75.3f),
            WeightEntry("2025-05-19", 74.3f),
            WeightEntry("2025-05-20", 73.3f),
            WeightEntry("2025-05-21", 72.3f),
            WeightEntry("2025-05-22", 71.3f),
            WeightEntry("2025-05-23", 72.5f),
            WeightEntry("2025-05-24", 71.0f),

            WeightEntry("2025-05-25", 72.0f),
            WeightEntry("2025-05-26", 73.0f),
            WeightEntry("2025-05-27", 74.0f),
            WeightEntry("2025-05-28", 75.0f),
            WeightEntry("2025-05-29", 76.0f),
            WeightEntry("2025-05-30", 74.0f),
            WeightEntry("2025-05-31", 75.0f),
            WeightEntry("2025-06-01", 73.0f),
            WeightEntry("2025-05-02", 72.0f),
            WeightEntry("2025-05-03", 71.0f),
            WeightEntry("2025-05-04", 72.0f),
            WeightEntry("2025-05-05", 71.0f),

            WeightEntry("2025-05-25", 72.0f),
            WeightEntry("2025-05-26", 73.0f),
            WeightEntry("2025-05-27", 74.0f),
            WeightEntry("2025-05-28", 75.0f),
            WeightEntry("2025-05-29", 76.0f),
            WeightEntry("2025-05-30", 74.0f),
            WeightEntry("2025-05-31", 75.0f),
            WeightEntry("2025-06-01", 73.0f),
            WeightEntry("2025-05-02", 72.0f),
            WeightEntry("2025-05-03", 71.0f),
            WeightEntry("2025-05-04", 72.0f),
            WeightEntry("2025-05-05", 71.0f),


        )

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
        }

        dataSet.setDrawValues(false) // отключить отображение значений на графике
        progressChart.data = LineData(dataSet)

        progressChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false

            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.labelRotationAngle = -30f
            xAxis.granularity = 1f
            xAxis.valueFormatter = DateAxisFormatter(weightHistory.map { it.date })

            axisLeft.setDrawGridLines(true)
            axisLeft.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)

            legend.isEnabled = false
            setTouchEnabled(false)
            invalidate()
        }

        val startWeight = weightHistory.first().weight
        val currentWeight = weightHistory.last().weight
        val goalWeight = 70f

        weightText.text = "Текущий вес: ${currentWeight.toInt()} кг"
        goalWeightText.text = "Цель: ${goalWeight.toInt()} кг"

        val delta = goalWeight - startWeight
        val progressDelta = currentWeight - startWeight
        val progressPercent = if (delta != 0f) (progressDelta / delta * 100).coerceIn(0f, 100f) else 0f
        val remaining = kotlin.math.abs(currentWeight - goalWeight)

//        progressPercentText.text = "Прогресс: ${progressPercent.toInt()} хахахаха%"
        weightLeftText.text = "Осталось: ${"%.1f".format(remaining)} кг"
    }

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
