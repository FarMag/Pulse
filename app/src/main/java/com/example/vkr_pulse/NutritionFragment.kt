package com.example.vkr_pulse

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vkr_pulse.data.FoodItem
import com.example.vkr_pulse.ui.dialogs.AddProductDialogFragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class NutritionFragment : Fragment() {

    private val client = OkHttpClient()

    private lateinit var caloriesLeftText: TextView
    private lateinit var caloriesEatenText: TextView
    private lateinit var caloriesGoalText: TextView

    private lateinit var carbsText: TextView
    private lateinit var proteinText: TextView
    private lateinit var fatText: TextView

    private val breakfastItems = mutableListOf<FoodItemEntry>()
    private val lunchItems = mutableListOf<FoodItemEntry>()
    private val dinnerItems = mutableListOf<FoodItemEntry>()
    private val snackItems = mutableListOf<FoodItemEntry>()

    data class FoodItemEntry(val item: FoodItem, val grams: Int)

    private lateinit var calorieLottie: LottieAnimationView
//    private var calorieGoal = 2200
    private var calorieGoal = 0
    private var currentCalories = 0
    private val maxLottieProgress = 0.875f

    private lateinit var waterAnimation: LottieAnimationView
    private lateinit var addWaterButton: ImageView
    private lateinit var waterText: TextView
    private lateinit var percentText: TextView
    private lateinit var infoButton: ImageView
    private lateinit var foodinfoButton: ImageView

    private var carbsMax = 0
    private var proteinMax = 0
    private var fatsMax = 0

    private var currentWater = 0  // в мл
//    private var waterGoal = 2000 // пример: рассчитывается из веса
    private var waterGoal = 0 // пример: рассчитывается из веса

    private var totalKcal = 0f
    private var totalProtein = 0f
    private var totalFats = 0f
    private var totalCarbs = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nutrition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val breakfastBlock = view.findViewById<View>(R.id.breakfastBlock)
        val lunchBlock = view.findViewById<View>(R.id.lunchBlock)
        val dinnerBlock = view.findViewById<View>(R.id.dinnerBlock)
        val snackBlock = view.findViewById<View>(R.id.snackBlock)


        breakfastBlock.findViewById<TextView>(R.id.mealTitle).text = "Завтрак"
        lunchBlock.findViewById<TextView>(R.id.mealTitle).text = "Обед"
        dinnerBlock.findViewById<TextView>(R.id.mealTitle).text = "Ужин"
        snackBlock.findViewById<TextView>(R.id.mealTitle).text = "Перекусы"

        // Завтрак
        breakfastBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener {
            showAddProductDialog("Завтрак")
        }

        // Обед
        lunchBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener {
            showAddProductDialog("Обед")
        }

        // Ужин
        dinnerBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener {
            showAddProductDialog("Ужин")
        }

        // Перекусы
        snackBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener {
            showAddProductDialog("Перекусы")
        }

        // чтобы изменить кол-во ккал за прием пищи
        // breakfastBlock.findViewById<TextView>(R.id.mealCalories).text = "300 ккал"


        caloriesLeftText = view.findViewById(R.id.caloriesLeft)
        caloriesEatenText = view.findViewById(R.id.caloriesEaten)
        caloriesGoalText = view.findViewById(R.id.caloriesGoal)

        carbsText = view.findViewById(R.id.carbsText)
        proteinText = view.findViewById(R.id.proteinText)
        fatText = view.findViewById(R.id.fatText)


        waterAnimation = view.findViewById(R.id.waterAnimation)
        addWaterButton = view.findViewById(R.id.addWaterButton)
        waterText = view.findViewById(R.id.waterAmountText)
        percentText = view.findViewById(R.id.waterPercentageText)
        infoButton = view.findViewById(R.id.infoButton)
        foodinfoButton = view.findViewById(R.id.foodinfoButton)

//        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
//        val accessToken = preferences.getString("access_jwt", null)
//        getData(accessToken.toString())


        // Удалено сохранение/загрузка из SharedPreferences

        // Пример расчёта цели (заменишь на данные из БД)
        val gender = "male"
        val age = 22
        val weightKg = 93f
        val heightCm = 180f
        val activityLevel = "medium" // beginner, medium, athlete
        val goal = "mass" // mass, losing, keeping, longevity

        // Вызов функции расчета
        val nutrition = calculateNutrition(
            gender = gender,
            age = age,
            weightKg = weightKg,
            heightCm = heightCm,
            activityLevel = activityLevel,
            goal = goal
        )
        calorieGoal = nutrition.calories
        carbsMax = nutrition.carbs
        proteinMax = nutrition.protein
        fatsMax = nutrition.fats


        val caloriesGoalText = view.findViewById<TextView>(R.id.caloriesGoal)

        val carbsProgress = view.findViewById<ProgressBar>(R.id.carbsProgress)
        val carbsText = view.findViewById<TextView>(R.id.carbsText)

        val proteinProgress = view.findViewById<ProgressBar>(R.id.proteinProgress)
        val proteinText = view.findViewById<TextView>(R.id.proteinText)

        val fatProgress = view.findViewById<ProgressBar>(R.id.fatProgress)
        val fatText = view.findViewById<TextView>(R.id.fatText)

        // Установим значения на основе расчета
        caloriesGoalText.text = nutrition.calories.toInt().toString()

        carbsProgress.max = nutrition.carbs.toInt()
        carbsProgress.progress = 0 // или nutrition.currentCarbGr если будет
        carbsText.text = "0 / ${nutrition.carbs.toInt()} г"

        proteinProgress.max = nutrition.protein.toInt()
        proteinProgress.progress = 0
        proteinText.text = "0 / ${nutrition.protein.toInt()} г"

        fatProgress.max = nutrition.fats.toInt()
        fatProgress.progress = 0
        fatText.text = "0 / ${nutrition.fats.toInt()} г"



        waterGoal = ((weightKg * 30).toInt()).coerceIn(1200, 4000)

        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)
        getData(accessToken.toString())
//        updateWaterUI()
//        updateCaloriesUI()

        addWaterButton.setOnClickListener {
            val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
            val accessToken = preferences.getString("access_jwt", null)
            currentWater += 250

            addUserWater(accessToken.toString())
            updateWaterUI()
            playAnimationToCurrentProgress()
        }

        // отображение подсказки при клике на info для ВОДЫ
        infoButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_water_info, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
            dialog.setCanceledOnTouchOutside(true)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        // отображение подсказки при клике на info для ЕДЫ
        foodinfoButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_food_info, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
            dialog.setCanceledOnTouchOutside(true)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        calorieLottie = view.findViewById(R.id.calorieLottie)
    }

    private fun getData(access_token: String) {
//        val url_user = getString(R.string.url_auth) + "getUserDataAndAge"
        val url_user = getString(R.string.url_auth) + "getUserData"
        val url_nutrition = getString(R.string.url_nutrition) + "getNutritionData"
        val url_progress = getString(R.string.url_progress) + "getUserWater"

        val formBody = FormBody.Builder()
            .add("access_token", access_token)
            .build()

        // Получаем данные пользователя
        val requestUser = Request.Builder()
            .url(url_user)
            .post(formBody)
            .build()

        client.newCall(requestUser).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    showCustomErrorToast("Ошибка получения данных пользователя")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    handleUserResponse(it)
                }
            }

            private fun handleUserResponse(response: Response) {
                if (!response.isSuccessful) {
                    handleErrorResponse(response.code)
                    return
                }

                val responseData = response.body?.string()
                requireActivity().runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        updateUIWithUserData(jsonResponse)

                        // После получения данных пользователя, получаем данные о питании
//                        getNutritionData(access_token)
                        getProgressData(access_token)
                    } catch (e: JSONException) {
                        showCustomErrorToast("Ошибка разбора данных пользователя")
                    }
                }
            }

            private fun getNutritionData(access_token: String) {
                val formBodyNutrition = FormBody.Builder()
                    .add("access_token", access_token)
                    .build()

                val requestNutrition = Request.Builder()
                    .url(url_nutrition)
                    .post(formBodyNutrition)
                    .build()

                client.newCall(requestNutrition).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        requireActivity().runOnUiThread {
                            showCustomErrorToast("Ошибка получения данных о питании")
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            handleNutritionResponse(it)
                        }
                    }

                    private fun handleNutritionResponse(response: Response) {
                        if (!response.isSuccessful) {
                            handleErrorResponse(response.code)
                            return
                        }

                        val responseData = response.body?.string()
                        requireActivity().runOnUiThread {
                            try {
                                val jsonResponse = JSONObject(responseData)
                                updateUIWithNutritionData(jsonResponse)

                                // После получения данных о питании, получаем данные о прогрессе
//                                getProgressData(access_token)
                            } catch (e: JSONException) {
                                showCustomErrorToast("Ошибка разбора данных о питании")
                            }
                        }
                    }
                })
            }

            private fun getProgressData(access_token: String) {
                val formBodyProgress = FormBody.Builder()
                    .add("access_token", access_token)
                    .build()

                val requestProgress = Request.Builder()
                    .url(url_progress)
                    .post(formBodyProgress)
                    .build()

                client.newCall(requestProgress).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        requireActivity().runOnUiThread {
                            showCustomErrorToast("Ошибка получения данных о прогрессе")
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            handleProgressResponse(it)
                        }
                    }

                    private fun handleProgressResponse(response: Response) {
                        if (!response.isSuccessful) {
                            handleErrorResponse(response.code)
                            return
                        }

                        val responseData = response.body?.string()
                        requireActivity().runOnUiThread {
                            try {
                                val jsonResponse = JSONObject(responseData)
                                updateWaterData(jsonResponse)

                                getNutritionData(access_token)
                            } catch (e: JSONException) {
                                showCustomErrorToast("Ошибка разбора данных о прогрессе")
                            }
                        }
                    }
                })
            }

            private fun handleErrorResponse(code: Int) {
                requireActivity().runOnUiThread {
                    when (code) {
                        400 -> showCustomErrorToast("Ошибка: ID не предоставлен")
                        404 -> showCustomErrorToast("Ошибка: Пользователь не найден")
                        500 -> showCustomErrorToast("Ошибка сервера, попробуйте позже")
                        else -> showCustomErrorToast("Неизвестная ошибка")
                    }
                }
            }
        })
    }

    private fun addUserWater(access_token: String) {
        val url = getString(R.string.url_progress) + "addUserWater"
        val formBody = FormBody.Builder()
            .add("access_token", access_token)
            .add("current_water", currentWater.toString())
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    showCustomErrorToast("Ошибка получения данных")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    // Используем функцию handleResponse для обработки ответа
                    handleResponse(response)
                }
            }

            private fun handleResponse(response: Response) {
                if (!response.isSuccessful) {
                    handleErrorResponse(response.code)
                    return
                }

                val responseData = response.body?.string()
                requireActivity().runOnUiThread {
                    try {
                        val jsonResponse = JSONObject(responseData)
                        // Обновляем пользовательский интерфейс с данными из jsonResponse
                        // Здесь предполагается наличие полей в JSON. Убедитесь, что они соответствуют вашим данным.
//                        updateUIWithData(jsonResponse)
                    } catch (e: JSONException) {
                        showCustomErrorToast("Ошибка разбора данных")
                    }
                }
            }

            private fun handleErrorResponse(code: Int) {
                requireActivity().runOnUiThread {
                    when (code) {
                        400 -> showCustomErrorToast("Ошибка: ID не предоставлен")
                        404 -> showCustomErrorToast("Ошибка: Пользователь не найден")
                        500 -> showCustomErrorToast("Ошибка сервера, попробуйте позже")
                        else -> showCustomErrorToast("Неизвестная ошибка")
                    }
                }
            }
        })
    }



    private fun updateUIWithNutritionData(jsonData: JSONObject) {

        view?.findViewById<ProgressBar>(R.id.profileLoading)?.visibility = View.GONE
        view?.findViewById<ScrollView>(R.id.profileContent)?.visibility = View.VISIBLE

        val calories = jsonData.getString("total_calories").toDouble().toInt()
        val protein = jsonData.getString("total_protein").toDouble().toInt()
        val fat = jsonData.getString("total_fat").toDouble().toInt()
        val carbohydrates = jsonData.getString("total_carbohydrates").toDouble().toInt()

//        caloriesEatenText.text = calories.toInt().toString()
        caloriesEatenText.text = calories.toString()
        proteinText.text = "${protein} / ${proteinMax} г"
        fatText.text = "${fat} / ${fatsMax} г"
        carbsText.text = "${carbohydrates} / ${carbsMax} г"


        // Предполагаем, что jsonData - это объект типа JSONObject
        val breakfast = jsonData.getJSONObject("breakfast")
        val lunch = jsonData.getJSONObject("lunch")
        val dinner = jsonData.getJSONObject("dinner")
        val snack = jsonData.getJSONObject("snack")

        val breakfastKcal = breakfast.getInt("calories")
        val lunchKcal = lunch.getInt("calories")
        val dinnerKcal = dinner.getInt("calories")
        val snackKcal = snack.getInt("calories")



        updateMealCalories(R.id.breakfastBlock, breakfastKcal)
        updateMealCalories(R.id.lunchBlock, lunchKcal)
        updateMealCalories(R.id.dinnerBlock, dinnerKcal)
        updateMealCalories(R.id.snackBlock, snackKcal)


//        val water = jsonData.getString("water")
//
//        private var totalKcal = 0f
//        private var totalProtein = 0f
//        private var totalFats = 0f
//        private var totalCarbs = 0f

        currentCalories = calories.toFloat().toInt()
//        currentWater = water.toInt()

        updateCaloriesUI()
//        updateWaterUI()

//        updateNutritionSummary()
    }

    private fun updateUIWithUserData(jsonData: JSONObject) {
//        val gender = "male"
//        val age = 22
//        val weightKg = 93f
//        val heightCm = 180f
//        val activityLevel = "medium" // beginner, medium, athlete
//        val goal = "mass" // mass, losing, keeping, longevity

        val gender = jsonData.getString("gender")
        val age = jsonData.getString("age").toInt()
        val weightKg = jsonData.getString("weight").toFloat()
        val heightCm = jsonData.getString("height").toFloat()
        val activityLevel = jsonData.getString("phis_train")
        val goal = jsonData.getString("target_phis")

//        currentWater = jsonData.getString("water").toInt()
//        updateWaterUI()

        val nutrition = calculateNutrition(
            gender = gender,
            age = age,
            weightKg = weightKg,
            heightCm = heightCm,
            activityLevel = activityLevel,
            goal = goal
        )
        calorieGoal = nutrition.calories
        carbsMax = nutrition.carbs
        proteinMax = nutrition.protein
        fatsMax = nutrition.fats

        waterGoal = ((weightKg * 30).toInt()).coerceIn(1200, 4000)
    }

    private fun updateWaterData(jsonData: JSONObject) {
        currentWater = jsonData.getString("water").toFloat().toInt()
        updateWaterUI()
    }

    private fun updateWaterUI() {
        val percent = ((currentWater.toFloat() / waterGoal) * 100).toInt().coerceAtMost(100)
        percentText.text = "$percent%"
        waterText.text = "$currentWater / $waterGoal мл"

        val progress = (percent / 100f * 0.65f).coerceAtMost(0.65f)
        waterAnimation.progress = progress
    }

    private fun playAnimationToCurrentProgress() {
        val percent = ((currentWater.toFloat() / waterGoal) * 100).toInt().coerceAtMost(100)
        val targetProgress = (percent / 100f) * 0.65f

        waterAnimation.cancelAnimation()
        waterAnimation.setMinAndMaxProgress(waterAnimation.progress, targetProgress)
        waterAnimation.playAnimation()
    }

    private fun playLottieCalorieAnimation(fromPercent: Float, toPercent: Float) {
        val startProgress = fromPercent * maxLottieProgress
        val endProgress = toPercent * maxLottieProgress

        calorieLottie.cancelAnimation()
        calorieLottie.setMinAndMaxProgress(startProgress, endProgress)
        calorieLottie.playAnimation()
    }


    // функция расчета BMR(базовый обмен веществ) и TDEE(фактическое потребление ккал)
    private fun updateCaloriesUI() {
        val remaining = (calorieGoal - currentCalories).coerceAtLeast(0)
        caloriesLeftText.text = remaining.toString()
        caloriesEatenText.text = currentCalories.toString()
        caloriesGoalText.text = calorieGoal.toString()
    }

    private fun calculateNutrition(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String
    ): NutritionPlan {

        // BMR по формуле Mifflin-St Jeor
        val bmr = if (gender == "male") {
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

        // Белок в г/кг
        val proteinPerKg = when (goal) {
            "losing" -> 2.2f
            "mass" -> 2.0f
            else -> 1.8f
        }
        val proteinGrams = proteinPerKg * weightKg
        val proteinCalories = proteinGrams * 4

        // Жиры — 1 г на 1 кг массы тела
        val fatsGrams = 1.0f * weightKg
        val fatsCalories = fatsGrams * 9

        // Углеводы — остаток калорий
        val remainingCalories = calories - (proteinCalories + fatsCalories)
        val carbsGrams = (remainingCalories / 4).coerceAtLeast(weightKg * 2f)

        return NutritionPlan(
            calories = calories.toInt(),
            protein = proteinGrams.toInt(),
            fats = fatsGrams.toInt(),
            carbs = carbsGrams.toInt()
        )
    }

    // При добавлении:
    private fun showAddProductDialog(mealType: String) {
        val dialog = AddProductDialogFragment.newInstance(mealType) { foodItem, grams, type ->
            val entry = FoodItemEntry(foodItem, grams)
            when (type) {
                "Завтрак" -> breakfastItems.add(entry)
                "Обед" -> lunchItems.add(entry)
                "Ужин" -> dinnerItems.add(entry)
                "Перекусы" -> snackItems.add(entry)
            }
            updateNutritionSummary()
        }
        dialog.show(childFragmentManager, "AddProductDialog")
    }

    private fun updateNutritionSummary() {
        val allItems = breakfastItems + lunchItems + dinnerItems + snackItems

//        var totalKcal = 0f
//        var totalProtein = 0f
//        var totalFats = 0f
//        var totalCarbs = 0f

        for (entry in allItems) {
            val factor = entry.grams / 100f
            totalKcal += entry.item.calories * factor
            totalProtein += entry.item.protein * factor
            totalFats += entry.item.fats * factor
            totalCarbs += entry.item.carbs * factor
        }

        // Обновляем основной блок калорий и БЖУ
        caloriesEatenText.text = totalKcal.toInt().toString()
        caloriesLeftText.text = (calorieGoal - totalKcal.toInt()).coerceAtLeast(0).toString()

        view?.findViewById<ProgressBar>(R.id.carbsProgress)?.progress = totalCarbs.toInt()
        view?.findViewById<ProgressBar>(R.id.proteinProgress)?.progress = totalProtein.toInt()
        view?.findViewById<ProgressBar>(R.id.fatProgress)?.progress = totalFats.toInt()

        view?.findViewById<TextView>(R.id.carbsText)?.text = "${totalCarbs.toInt()} / $carbsMax г"
        view?.findViewById<TextView>(R.id.proteinText)?.text = "${totalProtein.toInt()} / $proteinMax г"
        view?.findViewById<TextView>(R.id.fatText)?.text = "${totalFats.toInt()} / $fatsMax г"

        // Обновим калории по приёмам пищи
        updateMealCalories(R.id.breakfastBlock, calculateMealKcal(breakfastItems))
        updateMealCalories(R.id.lunchBlock, calculateMealKcal(lunchItems))
        updateMealCalories(R.id.dinnerBlock, calculateMealKcal(dinnerItems))
        updateMealCalories(R.id.snackBlock, calculateMealKcal(snackItems))

        // Лотти
        val prevPercent = calorieLottie.progress / maxLottieProgress
        val newPercent = (totalKcal / calorieGoal).coerceAtMost(1f)
        playLottieCalorieAnimation(prevPercent, newPercent)
    }


    private fun updateMealCalories(mealId: Int, kcal: Int) {
        val block = view?.findViewById<View>(mealId)
        val kcalText = block?.findViewById<TextView>(R.id.mealCalories)
        kcalText?.text = "$kcal ккал"
    }

    private fun calculateMealKcal(list: List<FoodItemEntry>): Int {
        return list.fold(0f) { acc, entry ->
            acc + entry.item.calories * (entry.grams / 100f)
        }.toInt()
    }

    fun showCustomSuccessToast(message: String) {
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

    fun showCustomErrorToast(message: String) {
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

// Класс-результат

    data class NutritionPlan(
        val calories: Int,
        val protein: Int,
        val fats: Int,
        val carbs: Int
    )


}
