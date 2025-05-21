//package com.example.vkr_pulse
//
//import android.graphics.Color
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.cardview.widget.CardView
//import androidx.fragment.app.Fragment
//import com.kizitonwose.calendar.core.*
//import com.kizitonwose.calendar.view.CalendarView
//import com.kizitonwose.calendar.view.ViewContainer
//import com.kizitonwose.calendar.view.MonthDayBinder
//import com.example.vkr_pulse.WorkoutTimerDialogFragment
//import java.time.DayOfWeek
//import java.time.LocalDate
//import java.time.YearMonth
//import java.util.Locale
//import androidx.navigation.fragment.findNavController
//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.FormBody
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.Response
//import org.json.JSONArray
//import java.io.IOException

//class TrainingFragment : Fragment() {
//
//    private lateinit var phraseTextView: TextView
//    private lateinit var phrases: Array<String>
//    private var currentPhraseIndex = 0
//    private lateinit var phraseRunnable: Runnable
//    private val handler = Handler(Looper.getMainLooper())
//
////    private val daysWithWorkout = mutableListOf<LocalDate>(
////        LocalDate.now().minusDays(1),
////        LocalDate.now().minusDays(3),
////        LocalDate.now().minusDays(5)
////    )
//
//    private val daysWithWorkout = mutableListOf<LocalDate>()
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_training, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        val startWorkoutButton = view.findViewById<Button>(R.id.startWorkoutButton)
//        startWorkoutButton.setOnClickListener {
//            val dialog = WorkoutTimerDialogFragment()
//            dialog.show(childFragmentManager, "WorkoutTimerDialog")
//        }
//
//
//        val trainingPlansCard = view.findViewById<CardView>(R.id.trainingPlansCard)
//        trainingPlansCard.setOnClickListener {
//            findNavController().navigate(R.id.trainingPlansFragment)
//        }
//
//
//        phraseTextView = view.findViewById(R.id.phraseTextView)
//        val card = view.findViewById<CardView>(R.id.knowledgeCard)
//        phrases = resources.getStringArray(R.array.sport_science_phrases)
//
//        card.setOnClickListener {
//            showNextPhraseWithAnimation()
//        }
//        startPhraseRotation()
//
//        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
//
//        val currentMonth = YearMonth.now()
//        val startMonth = currentMonth.minusMonths(12)
//        val endMonth = currentMonth.plusMonths(12)
//        val firstDayOfWeek = DayOfWeek.MONDAY
//
//        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
//        calendarView.scrollToMonth(currentMonth)
//
//        // Month header elements
//        val monthTitle = view.findViewById<TextView>(R.id.monthTitle)
//        val previousMonthButton = view.findViewById<ImageView>(R.id.previousMonth)
//        val nextMonthButton = view.findViewById<ImageView>(R.id.nextMonth)
//
//        // Функция обновления заголовка
//        fun updateMonthTitle(month: YearMonth) {
//            val formatter = java.time.format.DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
//            monthTitle.text = month.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
//        }
//
//        // При смене месяца — обновляем заголовок
//        calendarView.monthScrollListener = { month ->
//            updateMonthTitle(month.yearMonth)
//        }
//
//        // Инициализация заголовка сразу при открытии
//        updateMonthTitle(calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth)
//
//        // Кнопки переключения месяцев
//        previousMonthButton.setOnClickListener {
//            calendarView.findFirstVisibleMonth()?.let {
//                calendarView.scrollToMonth(it.yearMonth.minusMonths(1))
//            }
//        }
//        nextMonthButton.setOnClickListener {
//            calendarView.findFirstVisibleMonth()?.let {
//                calendarView.scrollToMonth(it.yearMonth.plusMonths(1))
//            }
//        }
//
//        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
//        val accessToken = preferences.getString("access_jwt", null)
//        getUserDateTraining(accessToken.toString())
//
//
//
////        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
////            override fun create(view: View) = DayViewContainer(view)
////
////            override fun bind(container: DayViewContainer, day: CalendarDay) {
////                container.textView.text = day.date.dayOfMonth.toString()
////
////                // Показать иконку тренировки, если день с тренировкой
////                container.iconView.visibility = if (day.date in daysWithWorkout && day.position == DayPosition.MonthDate) {
////                    View.VISIBLE
////                } else {
////                    View.GONE
////                }
////
////                // Подсветка текущей даты кружком
////                if (day.date == LocalDate.now() && day.position == DayPosition.MonthDate) {
////                    container.todayBackground.visibility = View.VISIBLE
////                } else {
////                    container.todayBackground.visibility = View.GONE
////                }
////
////                // Цвет текста для дней не из этого месяца
////                container.textView.setTextColor(
////                    if (day.position == DayPosition.MonthDate) Color.BLACK else Color.LTGRAY
////                )
////            }
////        }
//
//        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
//
//            override fun create(view: View) = DayViewContainer(view)
//
//
//            override fun bind(container: DayViewContainer, day: CalendarDay) {
//
//                container.textView.text = day.date.dayOfMonth.toString()
//
//
//                // Показать иконку тренировки, если день с тренировкой
//
//                container.iconView.visibility = if (day.date in daysWithWorkout && day.position == DayPosition.MonthDate) {
//
//                    View.VISIBLE
//
//                } else {
//
//                    View.GONE
//
//                }
//
//
//                // Подсветка текущей даты кружком
//
//                if (day.date == LocalDate.now() && day.position == DayPosition.MonthDate) {
//
//                    container.todayBackground.visibility = View.VISIBLE
//
//                } else {
//
//                    container.todayBackground.visibility = View.GONE
//
//                }
//
//
//                // Цвет текста для дней не из этого месяца
//
//                container.textView.setTextColor(
//
//                    if (day.position == DayPosition.MonthDate) Color.BLACK else Color.LTGRAY
//
//                )
//
//            }
//
//        }
//    }
//
//    private fun getUserDateTraining(accessToken: String) {
//
//        val client = OkHttpClient()
//
//
//        val url = getString(R.string.url_workouts) + "getUserDateTraining"
//
//
//        val formBody = FormBody.Builder()
//
//            .add("access_token", accessToken)
//
//            .build()
//
//
//        val request = Request.Builder()
//
//            .url(url)
//
//            .post(formBody)
//
//            .build()
//
//
//        client.newCall(request).enqueue(object : Callback {
//
//            override fun onFailure(call: Call, e: IOException) {
//
//                e.printStackTrace()
//
//                // Можно показать ошибку через Toast на UI-потоке
//
//            }
//
//
//            override fun onResponse(call: Call, response: Response) {
//
//                val data = response.body?.string()
//
//                if (response.isSuccessful && data != null) {
//
//                    requireActivity().runOnUiThread {
//
//                        // Парсим даты тренировок из JSON ответа
//
//                        parseWorkoutDates(data)
//
//                        // Обновление календаря с новыми датами
//
////                        calendarView.notifyCalendarChanged()
//
//                    }
//
//                } else {
//
//                    // Обработка ошибки
//
//                }
//
//            }
//
//        })
//
//    }
//
//    private fun parseWorkoutDates(jsonData: String) {
//
//        try {
//            val jsonArray = JSONArray(jsonData)
//            daysWithWorkout.clear() // Очистить старые данные
//
//            for (i in 0 until jsonArray.length()) {
//                val dateString = jsonArray.getString(i) // Предполагаем, что даты в виде строк
//                val date = LocalDate.parse(dateString) // Парсим строку как дату
//                daysWithWorkout.add(date) // Добавляем дату в список
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            // Обработка ошибки парсинга
//        }
//    }
//
//    private fun showNextPhraseWithAnimation() {
//        phraseTextView.animate()
//            .alpha(0f)
//            .translationY(20f)
//            .setDuration(300)
//            .withEndAction {
//                currentPhraseIndex = (currentPhraseIndex + 1) % phrases.size
//                phraseTextView.text = phrases[currentPhraseIndex]
//                phraseTextView.translationY = 20f
//                phraseTextView.alpha = 0f
//                phraseTextView.animate()
//                    .alpha(1f)
//                    .translationY(0f)
//                    .setDuration(500)
//                    .start()
//            }
//            .start()
//    }
//
//    private fun startPhraseRotation() {
//        phraseRunnable = object : Runnable {
//            override fun run() {
//                showNextPhraseWithAnimation()
//                handler.postDelayed(this, 10000)
//            }
//        }
//        handler.postDelayed(phraseRunnable, 10000)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        handler.removeCallbacks(phraseRunnable)
//    }
//
//    class DayViewContainer(view: View) : ViewContainer(view) {
//        val textView: TextView = view.findViewById(R.id.dayText)
//        val iconView: ImageView = view.findViewById(R.id.workoutIcon)
//        val todayBackground: View = view.findViewById(R.id.todayBackground)
//    }
//}































package com.example.vkr_pulse

import android.graphics.Color
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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.MonthDayBinder
import com.example.vkr_pulse.WorkoutTimerDialogFragment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import androidx.navigation.fragment.findNavController
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class TrainingFragment : Fragment() {

    private lateinit var phraseTextView: TextView
    private lateinit var phrases: Array<String>
    private var currentPhraseIndex = 0
    private lateinit var phraseRunnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private val daysWithWorkout = mutableListOf<LocalDate>()

    private lateinit var calendarView: CalendarView
    private lateinit var monthTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startWorkoutButton = view.findViewById<Button>(R.id.startWorkoutButton)
        startWorkoutButton.setOnClickListener {
            val dialog = WorkoutTimerDialogFragment()
            dialog.show(childFragmentManager, "WorkoutTimerDialog")
        }

        val trainingPlansCard = view.findViewById<CardView>(R.id.trainingPlansCard)
        trainingPlansCard.setOnClickListener {
            findNavController().navigate(R.id.trainingPlansFragment)
        }

        // Мотивационные фразы
        phraseTextView = view.findViewById(R.id.phraseTextView)
        val card = view.findViewById<CardView>(R.id.knowledgeCard)
        phrases = resources.getStringArray(R.array.sport_science_phrases)
        card.setOnClickListener { showNextPhraseWithAnimation() }
        startPhraseRotation()

        // Календарь
        calendarView = view.findViewById(R.id.calendarView)
        monthTitle = view.findViewById(R.id.monthTitle)
        val previousMonthButton = view.findViewById<ImageView>(R.id.previousMonth)
        val nextMonthButton = view.findViewById<ImageView>(R.id.nextMonth)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = DayOfWeek.MONDAY

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        fun updateMonthTitle(month: YearMonth) {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
            monthTitle.text = month.format(formatter).replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString()
            }
        }

        calendarView.monthScrollListener = { month ->
            updateMonthTitle(month.yearMonth)
        }
        updateMonthTitle(calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth)

        previousMonthButton.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.scrollToMonth(it.yearMonth.minusMonths(1))
            }
        }
        nextMonthButton.setOnClickListener {
            calendarView.findFirstVisibleMonth()?.let {
                calendarView.scrollToMonth(it.yearMonth.plusMonths(1))
            }
        }

        // День тренировки — кастомизация
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()
                container.iconView.visibility =
                    if (day.date in daysWithWorkout && day.position == DayPosition.MonthDate) View.VISIBLE else View.GONE

                container.todayBackground.visibility =
                    if (day.date == LocalDate.now() && day.position == DayPosition.MonthDate) View.VISIBLE else View.GONE

                container.textView.setTextColor(
                    if (day.position == DayPosition.MonthDate) Color.BLACK else Color.LTGRAY
                )
            }
        }

        // Получение токена и загрузка дат тренировок
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)
        if (!accessToken.isNullOrEmpty()) getUserDateTraining(accessToken)
    }

    // Получение дат тренировок с сервера
    private fun getUserDateTraining(accessToken: String) {
        val client = OkHttpClient()
        val url = getString(R.string.url_workouts) + "getUserDateTraining"
        val formBody = FormBody.Builder().add("access_token", accessToken).build()
        val request = Request.Builder().url(url).post(formBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка соединения", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()
                if (response.isSuccessful && data != null) {
                    requireActivity().runOnUiThread {
                        parseWorkoutDates(data)
                        // Обновляем только отмеченные дни
                        calendarView.findFirstVisibleMonth()?.let {
                            calendarView.notifyMonthChanged(it.yearMonth)
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Ошибка загрузки тренировок", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    // Парсим даты из JSON-ответа Python-сервера
    private fun parseWorkoutDates(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            val workoutsArray = jsonObject.optJSONArray("workouts")
            daysWithWorkout.clear()
            if (workoutsArray != null) {
                for (i in 0 until workoutsArray.length()) {
                    val workout = workoutsArray.getJSONObject(i)
                    val dateString = workout.optString("date")
                    if (dateString.isNotEmpty()) {
                        val date = LocalDate.parse(dateString)
                        daysWithWorkout.add(date)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка обработки данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNextPhraseWithAnimation() {
        phraseTextView.animate()
            .alpha(0f)
            .translationY(20f)
            .setDuration(300)
            .withEndAction {
                currentPhraseIndex = (currentPhraseIndex + 1) % phrases.size
                phraseTextView.text = phrases[currentPhraseIndex]
                phraseTextView.translationY = 20f
                phraseTextView.alpha = 0f
                phraseTextView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .start()
            }
            .start()
    }

    private fun startPhraseRotation() {
        phraseRunnable = object : Runnable {
            override fun run() {
                showNextPhraseWithAnimation()
                handler.postDelayed(this, 10000)
            }
        }
        handler.postDelayed(phraseRunnable, 10000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(phraseRunnable)
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
        val iconView: ImageView = view.findViewById(R.id.workoutIcon)
        val todayBackground: View = view.findViewById(R.id.todayBackground)
    }
}
