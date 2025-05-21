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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.vkr_pulse.data.FoodItem
import com.example.vkr_pulse.RecipesFragment
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
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator

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
    private lateinit var minusWaterButton: ImageView
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nutrition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainContent = view.findViewById<ScrollView>(R.id.mainContent)
        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loadingIndicator)
        mainContent.visibility = View.GONE
        loadingIndicator.visibility = View.VISIBLE

        val scrollView = requireView().findViewById<ScrollView>(R.id.mainContent)

        // 1. Инициализация view (без логики бизнес-расчётов!)
        val breakfastBlock = view.findViewById<View>(R.id.breakfastBlock)
        val lunchBlock = view.findViewById<View>(R.id.lunchBlock)
        val dinnerBlock = view.findViewById<View>(R.id.dinnerBlock)
        val snackBlock = view.findViewById<View>(R.id.snackBlock)

        setupMealExpandCollapse(breakfastBlock)
        setupMealExpandCollapse(lunchBlock)
        setupMealExpandCollapse(dinnerBlock)
        setupMealExpandCollapse(snackBlock)

        // Для примера, вручную:
        addMealProduct(breakfastBlock, "Овсянка с бананом", 210)
        addMealProduct(breakfastBlock, "Яйца", 140)
        addMealProduct(breakfastBlock, "Овсянка с бананом", 210)

        addMealProduct(dinnerBlock, "Овсянка с бананом", 210)
        addMealProduct(dinnerBlock, "Овсянка с бананом", 210)
        addMealProduct(dinnerBlock, "Овсянка с бананом", 210)


        addMealProduct(lunchBlock, "Овсянка с бананом", 210)
        addMealProduct(lunchBlock, "Овсянка с бананом", 210)


        breakfastBlock.findViewById<TextView>(R.id.mealTitle).text = "Завтрак"
        lunchBlock.findViewById<TextView>(R.id.mealTitle).text = "Обед"
        dinnerBlock.findViewById<TextView>(R.id.mealTitle).text = "Ужин"
        snackBlock.findViewById<TextView>(R.id.mealTitle).text = "Перекусы"

        breakfastBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener { showAddProductDialog("Завтрак") }
        lunchBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener { showAddProductDialog("Обед") }
        dinnerBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener { showAddProductDialog("Ужин") }
        snackBlock.findViewById<ImageView>(R.id.addMealButton).setOnClickListener { showAddProductDialog("Перекусы") }

        caloriesLeftText = view.findViewById(R.id.caloriesLeft)
        caloriesEatenText = view.findViewById(R.id.caloriesEaten)
        caloriesGoalText = view.findViewById(R.id.caloriesGoal)
        carbsText = view.findViewById(R.id.carbsText)
        proteinText = view.findViewById(R.id.proteinText)
        fatText = view.findViewById(R.id.fatText)

        waterAnimation = view.findViewById(R.id.waterAnimation)
        addWaterButton = view.findViewById(R.id.addWaterButton)
        minusWaterButton = view.findViewById(R.id.minusWaterButton)
        waterText = view.findViewById(R.id.waterAmountText)
        percentText = view.findViewById(R.id.waterPercentageText)
        infoButton = view.findViewById(R.id.infoButton)
        foodinfoButton = view.findViewById(R.id.foodinfoButton)
        calorieLottie = view.findViewById(R.id.calorieLottie)

        // 2. Установка дефолтных значений — можно пропустить, если дефолты выставлены в xml
        view.findViewById<ProgressBar>(R.id.carbsProgress)?.progress = 0
        view.findViewById<ProgressBar>(R.id.proteinProgress)?.progress = 0
        view.findViewById<ProgressBar>(R.id.fatProgress)?.progress = 0
        carbsText.text = "0 / 0 г"
        proteinText.text = "0 / 0 г"
        fatText.text = "0 / 0 г"
        caloriesEatenText.text = "0"
        caloriesGoalText.text = "0"
        caloriesLeftText.text = "0"

        // 3. Загрузи актуальные данные
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)
        getData(accessToken.toString())

        // 4. Логика кнопок/помощи/анимаций
        addWaterButton.setOnClickListener {
            val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
            val accessToken = preferences.getString("access_jwt", null)
            currentWater += 250
            addUserWater(accessToken.toString())
            updateWaterUI()
            playAnimationToCurrentProgress()
        }
        minusWaterButton.setOnClickListener {
            val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
            val accessToken = preferences.getString("access_jwt", null)
            currentWater -= 250
            addUserWater(accessToken.toString())
            updateWaterUI()
            playAnimationToCurrentProgress()
        }
        infoButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_water_info, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
            dialog.setCanceledOnTouchOutside(true)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
        foodinfoButton.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_food_info, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
            dialog.setCanceledOnTouchOutside(true)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        // ...
        val recipesCard = view.findViewById<View>(R.id.recipesCard)
        recipesCard.setOnClickListener {
            val navController = requireActivity().findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.recipesFragment)
        }

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

        // !!! Обновление текста
        caloriesEatenText.text = calories.toString()
        proteinText.text = "$protein / $proteinMax г"
        fatText.text = "$fat / $fatsMax г"
        carbsText.text = "$carbohydrates / $carbsMax г"

        // !!! Обновление прогрессбаров
        val carbsProgress = view?.findViewById<ProgressBar>(R.id.carbsProgress)
        carbsProgress?.max = carbsMax
        carbsProgress?.progress = carbohydrates.coerceAtMost(carbsMax)

        val proteinProgress = view?.findViewById<ProgressBar>(R.id.proteinProgress)
        proteinProgress?.max = proteinMax
        proteinProgress?.progress = protein.coerceAtMost(proteinMax)

        val fatProgress = view?.findViewById<ProgressBar>(R.id.fatProgress)
        fatProgress?.max = fatsMax
        fatProgress?.progress = fat.coerceAtMost(fatsMax)

        // Лотти-анимация (по желанию)
        val prevPercent = calorieLottie.progress / maxLottieProgress
        val newPercent = (calories / calorieGoal.toFloat()).coerceAtMost(1f)
        playLottieCalorieAnimation(prevPercent, newPercent)

        // По приёмам пищи (оставь как есть)
        val breakfast = jsonData.getJSONObject("breakfast")
        val lunch = jsonData.getJSONObject("lunch")
        val dinner = jsonData.getJSONObject("dinner")
        val snack = jsonData.getJSONObject("snack")

        updateMealCalories(R.id.breakfastBlock, breakfast.getInt("calories"))
        updateMealCalories(R.id.lunchBlock, lunch.getInt("calories"))
        updateMealCalories(R.id.dinnerBlock, dinner.getInt("calories"))
        updateMealCalories(R.id.snackBlock, snack.getInt("calories"))

        currentCalories = calories
        updateCaloriesUI()
        view?.findViewById<ScrollView>(R.id.mainContent)?.visibility = View.VISIBLE
        view?.findViewById<ProgressBar>(R.id.loadingIndicator)?.visibility = View.GONE

    }


    private fun updateUIWithUserData(jsonData: JSONObject) {

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

    fun playLottieCalorieAnimation(fromPercent: Float, toPercent: Float) {
        val startProgress = fromPercent.coerceIn(0f, 1f) * maxLottieProgress
        val endProgress = toPercent.coerceIn(0f, 1f) * maxLottieProgress

        calorieLottie.cancelAnimation()
        if (endProgress > startProgress + 0.0001f) {
            calorieLottie.setMinAndMaxProgress(startProgress, endProgress)
            calorieLottie.playAnimation()
        } else {
            calorieLottie.progress = endProgress
        }
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
            // 1. Не обновляй локальные breakfastItems/lunchItems — они не используются нигде для реального UI!
            // 2. После добавления отправь запрос на сервер (он уже реализован в AddProductDialogFragment)
            // 3. После успешного добавления — вызови повторную загрузку данных:
            val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
            val accessToken = preferences.getString("access_jwt", null)
            getData(accessToken.toString()) // Это вызовет полное обновление UI с сервера
        }
        dialog.show(childFragmentManager, "AddProductDialog")
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


    // добавление продуктов в прием пищи
    private fun addMealProduct(mealBlock: View, productName: String, calories: Int) {
        val productsList = mealBlock.findViewById<LinearLayout>(R.id.mealProductsList)
        // Удалить старые вью (если нужно обновить полностью)
        // productsList.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        val productView = inflater.inflate(R.layout.item_meal_product, productsList, false)
        productView.findViewById<TextView>(R.id.productName).text = productName
        productView.findViewById<TextView>(R.id.productCalories).text = "$calories ккал"
        productsList.addView(productView)
    }

    // анимация раскрытия продуктов за прием пиищ
    private fun setupMealExpandCollapse(mealBlock: View) {
        val productsList = mealBlock.findViewById<LinearLayout>(R.id.mealProductsList)
        val row = (mealBlock as ViewGroup).getChildAt(0)
        val nutritionMainLinear = requireView().findViewById<ViewGroup>(R.id.nutritionMainLinear)
        val arrow = mealBlock.findViewById<ImageView>(R.id.expandArrow)
        val scrollView = requireView().findViewById<ScrollView>(R.id.mainContent)

        var isExpanded = false

        row.setOnClickListener {
            isExpanded = !isExpanded

            if (!isExpanded) {
                val productsListHeight = productsList.height
                val scrollViewHeight = scrollView.height
                val contentHeight = scrollView.getChildAt(0).height
                val scrollY = scrollView.scrollY
                val distanceToBottom = contentHeight - (scrollY + scrollViewHeight)

                if (distanceToBottom < productsListHeight) {
                    val targetScrollY = (scrollY - (productsListHeight - distanceToBottom)).coerceAtLeast(0)
                    val animator = ValueAnimator.ofInt(scrollY, targetScrollY)
                    animator.duration = 400
                    animator.interpolator = DecelerateInterpolator()
                    animator.addUpdateListener { valueAnimator ->
                        scrollView.scrollTo(0, valueAnimator.animatedValue as Int)
                    }
                    animator.start()

                    // После скролла — запоминаем, куда проскроллили
                    productsList.postDelayed({
                        // Запоминаем scrollY после анимации
                        val fixedScrollY = scrollView.scrollY

                        androidx.transition.TransitionManager.beginDelayedTransition(
                            nutritionMainLinear,
                            androidx.transition.AutoTransition()
                        )
                        productsList.visibility = View.GONE
                        arrow?.animate()?.rotation(0f)?.setDuration(200)?.start()

                        // Через короткую задержку (после завершения TransitionManager) — выставляем scrollY вручную
                        productsList.postDelayed({
                            scrollView.scrollTo(0, fixedScrollY)
                        }, 150)
                    }, 420)
                } else {
                    // Места хватает, сворачиваем сразу:
                    androidx.transition.TransitionManager.beginDelayedTransition(nutritionMainLinear, androidx.transition.AutoTransition())
                    productsList.visibility = View.GONE
                    arrow?.animate()?.rotation(0f)?.setDuration(200)?.start()
                }
            } else {
                // Раскрываем:
                androidx.transition.TransitionManager.beginDelayedTransition(nutritionMainLinear, androidx.transition.AutoTransition())
                productsList.visibility = View.VISIBLE
                arrow?.animate()?.rotation(180f)?.setDuration(200)?.start()
            }
        }


    }



// Класс-результат

    data class NutritionPlan(
        val calories: Int,
        val protein: Int,
        val fats: Int,
        val carbs: Int
    )


}
