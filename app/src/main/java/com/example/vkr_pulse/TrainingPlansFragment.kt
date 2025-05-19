package com.example.vkr_pulse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class TrainingPlansFragment : Fragment() {

    private var userGoal: String = ""

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
            TrainingPlan("Силовая на всё тело", "3 раза в неделю, 60 мин", R.drawable.ic_mass),
            TrainingPlan("HIIT-интервалы", "2 раза в неделю, 30 мин", R.drawable.ic_mass),
            TrainingPlan("Функциональный тренинг", "3 раза в неделю, 45 мин", R.drawable.ic_mass)
        )
        val allPlans = listOf(
            TrainingPlan("Силовая на всё тело", "3 раза в неделю, 60 мин", R.drawable.ic_mass),
            TrainingPlan("HIIT-интервалы", "2 раза в неделю, 30 мин", R.drawable.ic_mass),
            TrainingPlan("Функциональный тренинг", "3 раза в неделю, 45 мин", R.drawable.ic_mass),
            TrainingPlan("Базовая выносливость", "4 раза в неделю, 40 мин", R.drawable.ic_mass),
            TrainingPlan("Тренировка на спину и пресс", "2 раза в неделю, 50 мин", R.drawable.ic_mass)
        )

        // По дефолту выбрана "Для меня"
        setButtonsActive(isRecommendedActive = true)

        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)
//        val userGoal = "" // "mass", "keeping", "losing"

        if (accessToken != null) {
            showPlans(accessToken, "recommended")
        }
//        showPlans(recommendedPlans)

        btnRecommended.setOnClickListener {
            setButtonsActive(isRecommendedActive = true)
//            showPlans(recommendedPlans)
            showPlans(accessToken.toString(), "recommended")
        }

        btnAll.setOnClickListener {
            setButtonsActive(isRecommendedActive = false)
            showPlans(accessToken.toString(), "all")
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

    // Определение класса TrainingPlan
//    data class TrainingPlan(
//        val name: String,
//        val desc: String,
//        val iconRes: Int // Предполагается, что это идентификатор ресурса изображения
//    )

    private fun fetchAndShowPlans(endpoint: String, goal: String) {
        val client = OkHttpClient()
        val url = getString(R.string.url_training_plan) + endpoint

        val formBody = FormBody.Builder()
            .add("goal", goal)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                showToast("Ошибка: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()
                if (response.isSuccessful && data != null) {
                    processResponse(data)
                } else {
                    showToast("Ошибка ответа от сервера: ${response.message}")
                }
            }
        })
    }

    private fun processResponse(data: String) {
        requireActivity().runOnUiThread {
            try {
                // Парсим JSON
                val jsonResponse = JSONArray(data)
                val plans = mutableListOf<TrainingPlan>()
                for (i in 0 until jsonResponse.length()) {
                    val planJson = jsonResponse.getJSONObject(i)
                    val name = planJson.getString("name")
                    val description = planJson.getString("description")
                    // Получаем значение цели
                    val goalString = planJson.getString("goal")

                    // Определяем соответствующий ресурс иконки в зависимости от значения цели
                    val iconRes = when (goalString) {
                        "mass" -> R.drawable.ic_mass
                        "losing" -> R.drawable.ic_losing
                        "keeping" -> R.drawable.ic_keeping
                        "longevity" -> R.drawable.ic_longevity
                        else -> R.drawable.ic_mass // Ресурс по умолчанию, если цель не распознана
                    }

                    val trainingPlan = TrainingPlan(name, description, iconRes)
                    plans.add(trainingPlan)
                }

                // Обновляем UI с новыми планами
                updatePlansUI(plans)

            } catch (e: JSONException) {
                e.printStackTrace()
                showToast("Ошибка парсинга данных")
            }
        }
    }

    private fun updatePlansUI(plans: List<TrainingPlan>) {
        // Очищаем контейнер перед добавлением новых планов
        plansContainer.removeAllViews()
        for (plan in plans) {
            // Инфлейтим представление для каждого плана
            val itemView = layoutInflater.inflate(R.layout.item_training_plan, plansContainer, false)
            itemView.findViewById<TextView>(R.id.planName).text = plan.name
            itemView.findViewById<TextView>(R.id.planDesc).text = plan.desc
            itemView.findViewById<ImageView>(R.id.planIcon).setImageResource(plan.iconRes)
            // Добавляем инфлейченное представление в контейнер
            plansContainer.addView(itemView)
        }
    }

    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }





    private fun showPlans(accessToken: String, planType: String) {
        val client = OkHttpClient()
        val url = getString(R.string.url_auth) + "getUserData"

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
                showToast("Ошибка: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()
                if (response.isSuccessful && data != null) {
                    requireActivity().runOnUiThread {
                        try {
                            // Парсим JSON
                            val jsonResponse = JSONObject(data)
                            userGoal = jsonResponse.getString("target_phis")
                            val planEndpoint = if (planType == "recommended") {
                                "getUserPersonalTrainingPlan"
                            } else {
                                "getAllTrainingPlan"
                            }
                            fetchAndShowPlans(planEndpoint, userGoal ?: "")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            showToast("Ошибка парсинга данных")
                        }
                    }
                } else {
                    showToast("Ошибка ответа от сервера: ${response.message}")
                }
            }
        })
    }

//    private fun showToast(message: String) {
//        requireActivity().runOnUiThread {
//            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//        }
//    }


    //    data class TrainingPlan(val name: String, val desc: String, val iconRes: Int)
data class TrainingPlan(val name: String, val desc: String, val iconRes: Int)
}
