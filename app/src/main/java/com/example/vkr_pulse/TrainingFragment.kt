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
import android.animation.ValueAnimator
import android.util.Log
import android.widget.LinearLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.kizitonwose.calendar.core.Week


class TrainingFragment : Fragment() {

    private lateinit var phraseTextView: TextView
    private lateinit var phrases: Array<String>
    private var currentPhraseIndex = 0
    private lateinit var phraseRunnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    private val daysWithWorkout = mutableListOf<LocalDate>()

    private lateinit var calendarView: CalendarView
    private lateinit var monthTitle: TextView

    private lateinit var calendarCard: CardView
    private var currentCalendarRows: Int = 0
    private var currentCalendarHeight: Int = 0

    private lateinit var factSummaries: Array<String>
    private lateinit var factDescriptions: Array<String>
    private lateinit var factSources: Array<String>
    private var currentFactIndex = 0

    private val workoutDetailsMap = mutableMapOf<LocalDate, Map<String, String>>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phraseTextView = view.findViewById(R.id.phraseTextView)
        val arrowLeft = view.findViewById<ImageView>(R.id.arrowLeft)
        val arrowRight = view.findViewById<ImageView>(R.id.arrowRight)
        val knowledgeCardCenter = view.findViewById<LinearLayout>(R.id.knowledgeCardCenter)
        val knowledgeCard = view.findViewById<CardView>(R.id.knowledgeCard)

        factSummaries = resources.getStringArray(R.array.fact_summaries)
        factDescriptions = resources.getStringArray(R.array.fact_descriptions)
        factSources = resources.getStringArray(R.array.fact_sources)

        // Показываем текущий факт
        phraseTextView.text = factSummaries[currentFactIndex]

        // Клик по центру открывает подробности
        knowledgeCardCenter.setOnClickListener {
            showKnowledgeDetailDialog(currentFactIndex)
        }
        // Клик по карточке (если надо, можно убрать — чтобы только центр был кликабельным)
        knowledgeCard.setOnClickListener {
            showKnowledgeDetailDialog(currentFactIndex)
        }

        // Стрелки — листаем влево и вправо
        arrowLeft.setOnClickListener {
            showFactWithAnimation(isNext = false)
        }
        arrowRight.setOnClickListener {
            showFactWithAnimation(isNext = true)
        }

        calendarCard = view.findViewById(R.id.calendarCard)
        calendarView = view.findViewById(R.id.calendarView)

        factSummaries = resources.getStringArray(R.array.fact_summaries)
        factDescriptions = resources.getStringArray(R.array.fact_descriptions)
        factSources = resources.getStringArray(R.array.fact_sources)
        knowledgeCard.setOnClickListener {
            showKnowledgeDetailDialog(currentFactIndex)
        }
        startFactRotation()

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
//        phraseTextView = view.findViewById(R.id.phraseTextView)
//        val card = view.findViewById<CardView>(R.id.knowledgeCard)
//        phrases = resources.getStringArray(R.array.sport_science_phrases)
//        card.setOnClickListener { showNextPhraseWithAnimation() }
//        startPhraseRotation()

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
                    if (workoutDetailsMap.containsKey(day.date) && day.position == DayPosition.MonthDate)
                        View.VISIBLE
                    else
                        View.GONE

                container.todayBackground.visibility =
                    if (day.date == LocalDate.now() && day.position == DayPosition.MonthDate) View.VISIBLE else View.GONE

                container.textView.setTextColor(
                    if (day.position == DayPosition.MonthDate) Color.BLACK else Color.LTGRAY
                )

                // Вот этот блок (сюда!)
                if (workoutDetailsMap.containsKey(day.date) && day.position == DayPosition.MonthDate) {
                    container.textView.setOnClickListener {
                        val details = workoutDetailsMap[day.date] ?: return@setOnClickListener
                        showWorkoutDetailsDialog(day.date, details)
                    }
                    container.textView.alpha = 1f
                } else {
                    container.textView.setOnClickListener(null)
                    container.textView.alpha = 0.7f
                }
            }
        }


        // Получение токена и загрузка дат тренировок
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)
        if (!accessToken.isNullOrEmpty()) getUserDateTraining(accessToken)

        calendarView.monthScrollListener = { month ->
            updateMonthTitle(month.yearMonth)
            animateCardHeight(month.weekDays.size)

        }

        calendarView.post {
            calendarView.findFirstVisibleMonth()?.let { firstMonth ->
                animateCardHeight(firstMonth.weekDays.size)
            }
        }
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
//                        Toast.makeText(requireContext(), "Ошибка загрузки тренировок", Toast.LENGTH_SHORT).show()
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
            workoutDetailsMap.clear()
            if (workoutsArray != null) {
                for (i in 0 until workoutsArray.length()) {
                    val workout = workoutsArray.getJSONObject(i)
                    val dateString = workout.optString("date")
                    if (dateString.isNotEmpty()) {
                        val date = LocalDate.parse(dateString)
                        workoutDetailsMap[date] = mapOf(
                            "duration" to workout.optString("duration", "0"),
                            "program" to workout.optString("program", "-"),
                            "type" to workout.optString("type", "-")
//                            "note" to workout.optString("note", "")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Ошибка обработки данных", Toast.LENGTH_SHORT).show()
        }
    }


    private fun animateCardHeight(newRows: Int) {
        val density = resources.displayMetrics.density

        val dayCellSize = 56 * density
        val headerHeight = 60 * density
        val verticalPadding = 60 * density

        val calendarHeight = (newRows * dayCellSize).toInt()
        val targetHeight = headerHeight.toInt() + (newRows * dayCellSize).toInt() + verticalPadding.toInt()

        Log.d(
            "CalendarAnim",
            "newRows = $newRows, cellSize = $dayCellSize, calendarHeight = $calendarHeight, headerHeight = $headerHeight, targetHeight = $targetHeight"
        )

        if (currentCalendarHeight == 0) {
            val lp = calendarCard.layoutParams
            lp.height = targetHeight
            calendarCard.layoutParams = lp
            currentCalendarHeight = targetHeight
            currentCalendarRows = newRows
            return
        }

        if (currentCalendarRows == newRows) return

        val animator = ValueAnimator.ofInt(currentCalendarHeight, targetHeight)
        animator.duration = 300
        animator.interpolator = FastOutSlowInInterpolator()
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            val lp = calendarCard.layoutParams
            lp.height = value
            calendarCard.layoutParams = lp
        }
        animator.start()

        currentCalendarHeight = targetHeight
        currentCalendarRows = newRows
    }

    // раскрытие модального окна с научными статьями
    private fun showKnowledgeDetailDialog(index: Int) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_knowledge_detail, null)

        val summaries = resources.getStringArray(R.array.fact_summaries)
        val descriptions = resources.getStringArray(R.array.fact_descriptions)
        val sources = resources.getStringArray(R.array.fact_sources)

        dialogView.findViewById<TextView>(R.id.knowledgeDialogTitle).text = summaries[index]
        dialogView.findViewById<TextView>(R.id.knowledgeDialogDescription).text = descriptions[index]
        dialogView.findViewById<TextView>(R.id.knowledgeDialogSource).text = "Источник: ${sources[index]}"

        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.closeDialogButton).setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_bg)
    }


    private fun showWorkoutDetailsDialog(date: LocalDate, details: Map<String, String>) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_workout_details, null)

        val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))
        dialogView.findViewById<TextView>(R.id.dateText).text = date.format(formatter)
        dialogView.findViewById<TextView>(R.id.programText).text = "Программа: ${details["program"] ?: "-"}"
        dialogView.findViewById<TextView>(R.id.typeText).text = "Тип: ${details["type"] ?: "-"}"
        dialogView.findViewById<TextView>(R.id.durationText).text = "${details["duration"] ?: "-"} мин"
        dialogView.findViewById<TextView>(R.id.noteText).text = details["note"] ?: "Нет заметок"

        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            alertDialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.editNoteButton).setOnClickListener {
            Toast.makeText(requireContext(), "Редактирование заметки в демо!", Toast.LENGTH_SHORT).show()
        }

        alertDialog.show()
        alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_bg)
    }


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

    private fun showNextFactWithAnimation() {
        phraseTextView.animate()
            .alpha(0f)
            .translationY(20f)
            .setDuration(300)
            .withEndAction {
                currentFactIndex = (currentFactIndex + 1) % factSummaries.size
                phraseTextView.text = factSummaries[currentFactIndex]
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

    private fun showFactWithAnimation(isNext: Boolean) {
        phraseTextView.animate()
            .alpha(0f)
            .translationY(20f)
            .setDuration(200)
            .withEndAction {
                // Перелистываем индекс
                currentFactIndex =
                    if (isNext)
                        (currentFactIndex + 1) % factSummaries.size
                    else
                        if (currentFactIndex == 0) factSummaries.size - 1 else currentFactIndex - 1
                phraseTextView.text = factSummaries[currentFactIndex]
                phraseTextView.translationY = 20f
                phraseTextView.alpha = 0f
                phraseTextView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .start()
            }
            .start()
    }

    private fun startFactRotation() {
        phraseRunnable = object : Runnable {
            override fun run() {
                showNextFactWithAnimation()
                handler.postDelayed(this, 10000)
            }
        }
        handler.postDelayed(phraseRunnable, 10000)
    }

//    private fun startPhraseRotation() {
//        phraseRunnable = object : Runnable {
//            override fun run() {
//                showNextPhraseWithAnimation()
//                handler.postDelayed(this, 10000)
//            }
//        }
//        handler.postDelayed(phraseRunnable, 10000)
//    }

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
