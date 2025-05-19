package com.example.vkr_pulse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class TrainingPlansFragment : Fragment() {

    private lateinit var btnRecommended: Button
    private lateinit var btnAll: Button
    private lateinit var plansContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_training_plans, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Кнопка закрытия (крестик)
        view.findViewById<ImageView>(R.id.closeButton)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnRecommended = view.findViewById(R.id.btnRecommended)
        btnAll = view.findViewById(R.id.btnAll)
        plansContainer = view.findViewById(R.id.trainingPlansContainer)

        // Пример списков тренировок
        val recommendedPlans = listOf(
            TrainingPlan("Силовая на всё тело", "3 раза в неделю, 60 мин", R.drawable.ic_strength),
            TrainingPlan("HIIT-интервалы", "2 раза в неделю, 30 мин", R.drawable.ic_strength),
            TrainingPlan("Функциональный тренинг", "3 раза в неделю, 45 мин", R.drawable.ic_strength)
        )
        val allPlans = listOf(
            TrainingPlan("Силовая на всё тело", "3 раза в неделю, 60 мин", R.drawable.ic_strength),
            TrainingPlan("HIIT-интервалы", "2 раза в неделю, 30 мин", R.drawable.ic_strength),
            TrainingPlan("Функциональный тренинг", "3 раза в неделю, 45 мин", R.drawable.ic_strength),
            TrainingPlan("Базовая выносливость", "4 раза в неделю, 40 мин", R.drawable.ic_strength),
            TrainingPlan("Тренировка на спину и пресс", "2 раза в неделю, 50 мин", R.drawable.ic_strength)
        )

        // По дефолту выбрана "Для меня"
        setButtonsActive(isRecommendedActive = true)
        showPlans(recommendedPlans)

        btnRecommended.setOnClickListener {
            setButtonsActive(isRecommendedActive = true)
            showPlans(recommendedPlans)
        }

        btnAll.setOnClickListener {
            setButtonsActive(isRecommendedActive = false)
            showPlans(allPlans)
        }
    }

    private fun setButtonsActive(isRecommendedActive: Boolean) {
        if (isRecommendedActive) {
            btnRecommended.setBackgroundResource(R.drawable.bg_button_primary)
            btnRecommended.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            btnAll.setBackgroundResource(R.drawable.bg_button_secondary)
            btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        } else {
            btnAll.setBackgroundResource(R.drawable.bg_button_primary)
            btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            btnRecommended.setBackgroundResource(R.drawable.bg_button_secondary)
            btnRecommended.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue))
        }
    }

    private fun showPlans(plans: List<TrainingPlan>) {
        plansContainer.removeAllViews()
        for (plan in plans) {
            val itemView = layoutInflater.inflate(R.layout.item_training_plan, plansContainer, false)
            itemView.findViewById<TextView>(R.id.planName).text = plan.name
            itemView.findViewById<TextView>(R.id.planDesc).text = plan.desc
            itemView.findViewById<ImageView>(R.id.planIcon).setImageResource(plan.iconRes)
            plansContainer.addView(itemView)
        }
    }

    data class TrainingPlan(val name: String, val desc: String, val iconRes: Int)
}
