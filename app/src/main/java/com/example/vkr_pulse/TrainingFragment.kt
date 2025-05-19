package com.example.vkr_pulse

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import androidx.navigation.fragment.findNavController

class TrainingFragment : Fragment() {

    private lateinit var phraseTextView: TextView
    private lateinit var phrases: Array<String>
    private var currentPhraseIndex = 0
    private lateinit var phraseRunnable: Runnable
    private val handler = Handler(Looper.getMainLooper())

    private val daysWithWorkout = listOf(
        LocalDate.now().minusDays(1),
        LocalDate.now().minusDays(3),
        LocalDate.now().minusDays(5)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_training, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val trainingPlansCard = view.findViewById<CardView>(R.id.trainingPlansCard)
        trainingPlansCard.setOnClickListener {
            findNavController().navigate(R.id.trainingPlansFragment)
        }


        phraseTextView = view.findViewById(R.id.phraseTextView)
        val card = view.findViewById<CardView>(R.id.knowledgeCard)
        phrases = resources.getStringArray(R.array.sport_science_phrases)

        card.setOnClickListener {
            showNextPhraseWithAnimation()
        }
        startPhraseRotation()

        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)

        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(12)
        val endMonth = currentMonth.plusMonths(12)
        val firstDayOfWeek = DayOfWeek.MONDAY

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        // Month header elements
        val monthTitle = view.findViewById<TextView>(R.id.monthTitle)
        val previousMonthButton = view.findViewById<ImageView>(R.id.previousMonth)
        val nextMonthButton = view.findViewById<ImageView>(R.id.nextMonth)

        // Функция обновления заголовка
        fun updateMonthTitle(month: YearMonth) {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
            monthTitle.text = month.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
        }

        // При смене месяца — обновляем заголовок
        calendarView.monthScrollListener = { month ->
            updateMonthTitle(month.yearMonth)
        }

        // Инициализация заголовка сразу при открытии
        updateMonthTitle(calendarView.findFirstVisibleMonth()?.yearMonth ?: currentMonth)

        // Кнопки переключения месяцев
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



        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)

            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.textView.text = day.date.dayOfMonth.toString()

                // Показать иконку тренировки, если день с тренировкой
                container.iconView.visibility = if (day.date in daysWithWorkout && day.position == DayPosition.MonthDate) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                // Подсветка текущей даты кружком
                if (day.date == LocalDate.now() && day.position == DayPosition.MonthDate) {
                    container.todayBackground.visibility = View.VISIBLE
                } else {
                    container.todayBackground.visibility = View.GONE
                }

                // Цвет текста для дней не из этого месяца
                container.textView.setTextColor(
                    if (day.position == DayPosition.MonthDate) Color.BLACK else Color.LTGRAY
                )
            }
        }
    }

    class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.dayText)
        val iconView: ImageView = view.findViewById(R.id.workoutIcon)
        val todayBackground: View = view.findViewById(R.id.todayBackground)
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
}
