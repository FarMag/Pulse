package com.example.vkr_pulse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.*
import java.io.IOException
import android.content.SharedPreferences
import android.provider.ContactsContract.CommonDataKinds.Email
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import androidx.compose.ui.text.font.FontWeight
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment() {

    private val client = OkHttpClient()

//    private val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
//    private val accessToken = preferences.getString("access_jwt", null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.logoutButton).setOnClickListener {
            logoutAccount()
//            Toast.makeText(requireContext(), "Выход из аккаунта", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.editWeightButton).setOnClickListener {
            showEditWeightDialog()
        }

        view.findViewById<Button>(R.id.editPersonalDataButton).setOnClickListener {
            showEditUserDataDialog()
        }

//        val userId = getUserId() // Предполагая, что у вас есть метод для получения ID пользователя
//        if (userId != null) {
//            getData(userId)
//        }

        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)
        getData(accessToken.toString())
    }


//    private fun getData(id: String) {
//        val url = getString(R.string.url_auth) + "getdata"
//        val formBody = FormBody.Builder()
//            .add("id", id)
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
//                    if (!response.isSuccessful) {
//                        requireActivity().runOnUiThread {
//                            Toast.makeText(requireContext(), "Ошибка: ${response.code}", Toast.LENGTH_SHORT).show()
//                        }
//                        return
//                    }
//
//                    val responseData = response.body?.string()
//                    requireActivity().runOnUiThread {
//                        // Здесь обновите UI с полученными данными
//                        Toast.makeText(requireContext(), "Данные получены: $responseData", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        })
//    }

    private fun getData(access_token: String) {
        val url = getString(R.string.url_auth) + "getUserData"
        val formBody = FormBody.Builder()
            .add("access_token", access_token)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка получения данных", Toast.LENGTH_SHORT).show()
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
                            updateUIWithData(jsonResponse)
                        } catch (e: JSONException) {
                            Toast.makeText(requireContext(), "Ошибка разбора данных", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            private fun handleErrorResponse(code: Int) {
                requireActivity().runOnUiThread {
                    when (code) {
                        400 -> Toast.makeText(requireContext(), "Ошибка: ID не предоставлен", Toast.LENGTH_SHORT).show()
                        404 -> Toast.makeText(requireContext(), "Ошибка: Пользователь не найден", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(requireContext(), "Ошибка сервера, попробуйте позже", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(requireContext(), "Неизвестная ошибка: $code", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

//    private fun updateData(id: String, username: String, height: String,
//                           birth_date: String, targetWeight: String, activityLevelInput: String,
//                           trainingGoalInput: String) {
//
//        val activityLevel = when (activityLevelInput) {
//            "Новичок (стаж менее 1 года)" -> "beginner"
//            "Умеренный (стаж от 1 до 3 лет)" -> "medium"
//            "Атлет (стаж более 3 лет)" -> "athlete"
//            else -> "ERROR" // Предпочтительно указывать значение по умолчанию
//        }
//
//        val trainingGoal = when (trainingGoalInput) {
//            "Похудение" -> "losing"
//            "Набор мышечной массы" -> "mass"
//            "Поддержание формы" -> "keeping"
//            "Спортивное долголетие" -> "longevity"
//            else -> "ERROR" // Предпочтительно указывать значение по умолчанию
//        }
//
//        val url = getString(R.string.url_auth) + "updateUserInformation"
//        val formBody = FormBody.Builder()
//            .add("id", id)
//            .add("username", username)
//            .add("height", height)
//            .add("birth_date", birth_date)
//            .add("target_weight", targetWeight)
//            .add("phis_train", activityLevel)
//            .add("target_phis", trainingGoal)
//            .build()
//
//        val request = Request.Builder()
//                .url(url)
//                .post(formBody)
//                .build()
//
//            client.newCall(request).enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    e.printStackTrace()
//                    requireActivity().runOnUiThread {
//                        Toast.makeText(requireContext(), "Ошибка получения данных", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    response.use {
//                        // Используем функцию handleResponse для обработки ответа
//                        handleResponse(response)
//                    }
//                }
//
//                private fun handleResponse(response: Response) {
//                    if (!response.isSuccessful) {
//                        handleErrorResponse(response.code)
//                        return
//                    }
//
//                    val responseData = response.body?.string()
//                    requireActivity().runOnUiThread {
//                        try {
//                            val jsonResponse = JSONObject(responseData)
//                            // Обновляем пользовательский интерфейс с данными из jsonResponse
//                            // Здесь предполагается наличие полей в JSON. Убедитесь, что они соответствуют вашим данным.
//    //                        updateUIWithData(jsonResponse)
//                        } catch (e: JSONException) {
//                            Toast.makeText(requireContext(), "Ошибка разбора данных", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//
//                private fun handleErrorResponse(code: Int) {
//                    requireActivity().runOnUiThread {
//                        when (code) {
//                            400 -> Toast.makeText(requireContext(), "Ошибка: ID не предоставлен", Toast.LENGTH_SHORT).show()
//                            404 -> Toast.makeText(requireContext(), "Ошибка: Пользователь не найден", Toast.LENGTH_SHORT).show()
//                            500 -> Toast.makeText(requireContext(), "Ошибка сервера, попробуйте позже", Toast.LENGTH_SHORT).show()
//                            else -> Toast.makeText(requireContext(), "Неизвестная ошибка: $code", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                }
//            })
//    }

    // Первая версия функции для более полного обновления данных пользователя
    private fun updateData(accessToken: String, username: String, height: String,
                           birth_date: String, targetWeight: String, activityLevelInput: String,
                           trainingGoalInput: String) {

        val activityLevel = when (activityLevelInput) {
            "Новичок (стаж менее 1 года)" -> "beginner"
            "Умеренный (стаж от 1 до 3 лет)" -> "medium"
            "Атлет (стаж более 3 лет)" -> "athlete"
            else -> "ERROR" // Предпочтительно указывать значение по умолчанию
        }

        val trainingGoal = when (trainingGoalInput) {
            "Похудение" -> "losing"
            "Набор мышечной массы" -> "mass"
            "Поддержание формы" -> "keeping"
            "Спортивное долголетие" -> "longevity"
            else -> "ERROR" // Предпочтительно указывать значение по умолчанию
        }

        val url = getString(R.string.url_auth) + "updateUserInformation"
        val formBody = FormBody.Builder()
            .add("access_token", accessToken)
            .add("username", username)
            .add("height", height)
            .add("birth_date", birth_date)
            .add("target_weight", targetWeight)
            .add("phis_train", activityLevel)
            .add("target_phis", trainingGoal)
            .build()

        makeRequest(url, formBody)
    }

    // Вторая версия функции для упрощённого обновления веса
    private fun updateData(accessToken: String, weight: String) {
        val urlUserInformation = getString(R.string.url_auth) + "updateUserInformation"
        val formBodyUserInformation = FormBody.Builder()
            .add("access_token", accessToken)
            .add("weight", weight)
            .build()

        makeRequest(urlUserInformation, formBodyUserInformation)

        val urlUserCurrentWeight = getString(R.string.url_progress) + "updateUserCurrentWeight"
        val formBodyUserCurrentWeight = FormBody.Builder()
            .add("access_token", accessToken)
            .add("weight", weight)
            .build()

        makeRequest(urlUserCurrentWeight, formBodyUserCurrentWeight)
    }

    // Функция для выполнения запроса
    private fun makeRequest(url: String, formBody: RequestBody) {
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка получения данных", Toast.LENGTH_SHORT).show()
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
                        // Проверяем наличие поля "answer" в jsonResponse
                        val answer = jsonResponse.optString("answer")
                        if (answer == "Success") {
                            Toast.makeText(requireContext(), "Информация успешно обновлена!", Toast.LENGTH_SHORT).show()
//                            showSuccessToast()
                        } else {
                            // Обработка других posibles состояний ответа
                            Toast.makeText(requireContext(), answer ?: "Неизвестный ответ", Toast.LENGTH_SHORT).show()
                        }
                        // Здесь вы можете обновить пользовательский интерфейс с данными из jsonResponse
                        // updateUIWithData(jsonResponse)
                    } catch (e: JSONException) {
                        Toast.makeText(requireContext(), "Ошибка разбора данных", Toast.LENGTH_SHORT).show()
                    }
                }
            }


            private fun handleErrorResponse(code: Int) {
                requireActivity().runOnUiThread {
                    when (code) {
                        400 -> Toast.makeText(requireContext(), "Ошибка: ID не предоставлен", Toast.LENGTH_SHORT).show()
                        404 -> Toast.makeText(requireContext(), "Ошибка: Пользователь не найден", Toast.LENGTH_SHORT).show()
                        500 -> Toast.makeText(requireContext(), "Ошибка сервера, попробуйте позже", Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(requireContext(), "Неизвестная ошибка: $code", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }


    private fun showEditWeightDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_weight, null)
        val editText = dialogView.findViewById<EditText>(R.id.weightInput)
        val saveButton = dialogView.findViewById<Button>(R.id.saveWeightButton)

        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val weightStr = editText.text.toString()
            if (weightStr.isNotBlank()) {
                val weight = weightStr.toIntOrNull()

                if (weight != null) {
                    val minWeight = resources.getInteger(R.integer.min_weight)
                    val maxWeight = resources.getInteger(R.integer.max_weight)

                    if (weight < minWeight || weight > maxWeight) {
                        editText.error = "Вес должен быть в пределах $minWeight - $maxWeight кг"
                    } else {
                        view?.findViewById<TextView>(R.id.weightText)?.text = "Текущий вес: $weight кг"
                        try {
                            updateData(accessToken.toString(),
                                weight.toString()
                            )
                        } catch (e: JSONException) {
                            Toast.makeText(requireContext(), "Ошибка сохранение данных", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
                } else {
                    editText.error = "Введите корректное число"
                }
            } else {
                editText.error = "Поле не может быть пустым"
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    private fun updateUIWithData(jsonResponse: JSONObject) {
        val defaultUsername = "Не указано"
        val defaultEmail = "Не указана"
        val defaultHeight = "Не указано см"
        val defaultBirthdate = "Не указана"
        val defaultTargetWeight = "Не указан кг"
        val defaultActivityLevel = "Не указан"
        val defaultTrainingGoal = "Не указан"
        val defaultWeight = "Не указан"

        // Сохранение ссылок на элементы UI
        val nameTextView = view?.findViewById<TextView>(R.id.userName)
        val nameTextEmail = view?.findViewById<TextView>(R.id.emailText)
        val heightTextView = view?.findViewById<TextView>(R.id.heightText)
        val birthdateTextView = view?.findViewById<TextView>(R.id.birthdateText)
        val targetWeightTextView = view?.findViewById<TextView>(R.id.targetWeightText)
        val weightTextView = view?.findViewById<TextView>(R.id.weightText)
        val activityLevelTextView = view?.findViewById<TextView>(R.id.activityLevelText)
        val trainingGoalTextView = view?.findViewById<TextView>(R.id.trainingGoalText)
        val profileImageView = view?.findViewById<ImageView>(R.id.profileImage)

        val gender = jsonResponse.optString("gender")
        val profileImageResource = when (gender) {
            "male" -> R.drawable.logo_man
            "female" -> R.drawable.logo_woman
            else -> R.drawable.logo_man // Можно заменить на изображение по умолчанию
        }

// Установим изображение в ImageView
        profileImageView?.setImageResource(profileImageResource)




        // Получение username
        nameTextView?.text = jsonResponse.optString("username", defaultUsername)

        // Получение почты
        nameTextEmail?.text = jsonResponse.optString("email", defaultEmail)

        // Обработка значения роста
        val height = jsonResponse.optString("height", defaultHeight)
        heightTextView?.text = "Рост: ${if (height != defaultHeight) String.format("%.0f", height.toFloat()) else defaultHeight} см"





        // Обработка даты рождения
//        birthdateTextView?.text = "Дата рождения: ${jsonResponse.optString("birth_date", defaultBirthdate)}"
//        val birthDateString = "Дата рождения: ${jsonResponse.optString("birth_date", defaultBirthdate)}"
        val birthDateString = jsonResponse.optString("birth_date", defaultBirthdate)
//        val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        val inputFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
//        val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH)

        try {
            // Парсим строку даты и форматируем в нужный формат
            val date = inputFormat.parse(birthDateString)
            val formattedDate = outputFormat.format(date)

            // Устанавливаем отформатированную дату в текстовое поле
            birthdateTextView?.text = "Дата рождения: $formattedDate"
        } catch (e: Exception) {
            // Обработка исключения в случае неудачи парсинга
            birthdateTextView?.text = "Ошибка: ${e.message}"
        }




        // Обработка целевого веса
        val targetWeight = jsonResponse.optString("target_weight", defaultTargetWeight)
        targetWeightTextView?.text = "Целевой вес: ${if (targetWeight != defaultTargetWeight) String.format("%.0f", targetWeight.toFloat()) else defaultTargetWeight} кг"

        // Обработка уровня активности с заменой значений ENUM
        val activityLevel = jsonResponse.optString("phis_train", defaultActivityLevel)
        val activityLevelText = when (activityLevel) {
            "beginner" -> "Новичок (стаж менее 1 года)"
            "medium" -> "Умеренный (стаж от 1 до 3 лет)"
            "athlete" -> "Атлет (стаж более 3 лет)"  // только пример, вы можете заменить на русский аналог
            else -> defaultActivityLevel
        }
        activityLevelTextView?.text = "Уровень активности: $activityLevelText"

        // Обработка цели тренировки с заменой значений ENUM
        val trainingGoal = jsonResponse.optString("target_phis", defaultTrainingGoal)
        val trainingGoalText = when (trainingGoal) {
            "losing" -> "Похудение"
            "mass" -> "Набор мышечной массы"
            "keeping" -> "Поддержание формы"
            "longevity" -> "Спортивное долголетие"
            else -> defaultTrainingGoal
        }
        trainingGoalTextView?.text = "Цель: $trainingGoalText"

        // Обработка целевого веса
        val weight = jsonResponse.optString("weight", defaultWeight)
        weightTextView?.text = "Текущий вес: ${if (targetWeight != defaultWeight) String.format("%.0f", weight.toFloat()) else defaultWeight} кг"
    }




//    private fun showEditUserDataDialog() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user_data, null)
//
//        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
//        val heightInput = dialogView.findViewById<EditText>(R.id.heightInput)
//        val birthInput = dialogView.findViewById<EditText>(R.id.birthInput)
//        val targetWeightInput = dialogView.findViewById<EditText>(R.id.targetWeightInput)
//        val activityPicker = dialogView.findViewById<TextView>(R.id.activityLevelPicker)
//        val goalPicker = dialogView.findViewById<TextView>(R.id.trainingGoalPicker)
//        val saveButton = dialogView.findViewById<Button>(R.id.saveUserDataButton)
//
//        // Установка текущих значений
//        nameInput.setText(view?.findViewById<TextView>(R.id.userName)?.text?.toString())
//        heightInput.setText(view?.findViewById<TextView>(R.id.heightText)?.text?.toString()?.filter { it.isDigit() })
//        birthInput.setText(view?.findViewById<TextView>(R.id.birthdateText)?.text?.toString()?.substringAfter(": ")?.trim())
//        targetWeightInput.setText(view?.findViewById<TextView>(R.id.targetWeightText)?.text?.toString()?.filter { it.isDigit() })
//        activityPicker.text = view?.findViewById<TextView>(R.id.activityLevelText)?.text?.toString()?.substringAfter(": ")?.trim()
//        goalPicker.text = view?.findViewById<TextView>(R.id.trainingGoalText)?.text?.toString()?.substringAfter(": ")?.trim()
//
//        birthInput.addTextChangedListener(object : TextWatcher {
//            var isUpdating = false
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                if (isUpdating) return
//                isUpdating = true
//
//                // Удаляем все точки из строки
//                val cleaned = s.toString().replace(".", "")
//
//                // Ограничиваем ввод до 8 символов
//                if (cleaned.length > 8) {
//                    birthInput.setText(cleaned.substring(0, 8))
//                    birthInput.setSelection(8) // Устанавливаем курсор в конец
//                    isUpdating = false
//                    return
//                }
//
//                // Форматируем дату в виде DD.MM.YYYY
//                val formatted = StringBuilder()
//                for (i in cleaned.indices) {
//                    if (i == 2 || i == 4) {
//                        formatted.append(".")
//                    }
//                    formatted.append(cleaned[i])
//                }
//
//                birthInput.setText(formatted.toString())
//                birthInput.setSelection(formatted.length) // Устанавливаем курсор в конец
//                isUpdating = false
//            }
//
//            override fun afterTextChanged(s: Editable?) {}
//        })
//
//        // Обработчики кастомного выбора
//        activityPicker.setOnClickListener {
//            showChoiceDialog("Выберите уровень активности", resources.getStringArray(R.array.activity_levels)) {
//                activityPicker.text = it
//            }
//        }
//
//        goalPicker.setOnClickListener {
//            showChoiceDialog("Выберите цель тренировок", resources.getStringArray(R.array.training_goals)) {
//                goalPicker.text = it
//            }
//        }
//
//        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
//            .setView(dialogView)
//            .create()
//
//        saveButton.setOnClickListener {
//            val height = heightInput.text.toString().toIntOrNull()
//            val targetWeight = targetWeightInput.text.toString().toIntOrNull()
//
//            // Проверка на корректность роста
//            if (height == null || height < resources.getInteger(R.integer.min_height)
//                || height > resources.getInteger(R.integer.max_height)) {
//                Toast.makeText(context, "Введите корректный рост (от 130 до 220 см)", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // Проверка на корректность веса
//            if (targetWeight == null || targetWeight < resources.getInteger(R.integer.min_weight)
//                || targetWeight > resources.getInteger(R.integer.max_weight)) {
//                Toast.makeText(context, "Введите корректный целевой вес (от 40 до 150 кг)", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (!isValidDate(birthInput.text.toString())) {
//                Toast.makeText(context, "Введите корректную дату рождения", Toast.LENGTH_SHORT)
//                    .show()
//            }
//
//            view?.findViewById<TextView>(R.id.userName)?.text = nameInput.text.toString()
//            view?.findViewById<TextView>(R.id.heightText)?.text = "Рост: ${height} см"
//            view?.findViewById<TextView>(R.id.birthdateText)?.text = "Дата рождения: ${birthInput.text}"
//            view?.findViewById<TextView>(R.id.targetWeightText)?.text = "Целевой вес: ${targetWeight} кг"
//            view?.findViewById<TextView>(R.id.activityLevelText)?.text = "Уровень активности: ${activityPicker.text}"
//            view?.findViewById<TextView>(R.id.trainingGoalText)?.text = "Цель: ${goalPicker.text}"
//
//            try {
//                updateData(getUserId().toString(),
//                    nameInput.text.toString(),
//                    height.toString(),
//                    birthInput.text.toString(),
//                    targetWeight.toString(),
//                    activityPicker.text.toString(),
//                    goalPicker.text.toString())
//            } catch (e: JSONException) {
//                Toast.makeText(requireContext(), "Ошибка сохранение данных", Toast.LENGTH_SHORT).show()
//            }
//
//            dialog.dismiss()
//        }
//
//
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//        dialog.show()
//    }

    private fun showEditUserDataDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_user_data, null)

        val nameInput = dialogView.findViewById<EditText>(R.id.nameInput)
        val heightInput = dialogView.findViewById<EditText>(R.id.heightInput)
        val birthInput = dialogView.findViewById<EditText>(R.id.birthInput)
        val targetWeightInput = dialogView.findViewById<EditText>(R.id.targetWeightInput)
        val activityPicker = dialogView.findViewById<TextView>(R.id.activityLevelPicker)
        val goalPicker = dialogView.findViewById<TextView>(R.id.trainingGoalPicker)
        val saveButton = dialogView.findViewById<Button>(R.id.saveUserDataButton)

        // Установка текущих значений
        nameInput.setText(view?.findViewById<TextView>(R.id.userName)?.text?.toString())
        heightInput.setText(view?.findViewById<TextView>(R.id.heightText)?.text?.toString()?.filter { it.isDigit() })
        birthInput.setText(view?.findViewById<TextView>(R.id.birthdateText)?.text?.toString()?.substringAfter(": ")?.trim())
        targetWeightInput.setText(view?.findViewById<TextView>(R.id.targetWeightText)?.text?.toString()?.filter { it.isDigit() })
        activityPicker.text = view?.findViewById<TextView>(R.id.activityLevelText)?.text?.toString()?.substringAfter(": ")?.trim()
        goalPicker.text = view?.findViewById<TextView>(R.id.trainingGoalText)?.text?.toString()?.substringAfter(": ")?.trim()

        birthInput.addTextChangedListener(object : TextWatcher {
            var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                isUpdating = true

                // Удаляем все точки из строки
                val cleaned = s.toString().replace(".", "")

                // Ограничиваем ввод до 8 символов
                if (cleaned.length > 8) {
                    birthInput.setText(cleaned.substring(0, 8))
                    birthInput.setSelection(8) // Устанавливаем курсор в конец
                    isUpdating = false
                    return
                }

                // Форматируем дату в виде DD.MM.YYYY
                val formatted = StringBuilder()
                for (i in cleaned.indices) {
                    if (i == 2 || i == 4) {
                        formatted.append(".")
                    }
                    formatted.append(cleaned[i])
                }

                birthInput.setText(formatted.toString())
                birthInput.setSelection(formatted.length) // Устанавливаем курсор в конец
                isUpdating = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработчики кастомного выбора
        activityPicker.setOnClickListener {
            showChoiceDialog("Выберите уровень активности", resources.getStringArray(R.array.activity_levels)) {
                activityPicker.text = it
            }
        }

        goalPicker.setOnClickListener {
            showChoiceDialog("Выберите цель тренировок", resources.getStringArray(R.array.training_goals)) {
                goalPicker.text = it
            }
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val height = heightInput.text.toString().toIntOrNull()
            val targetWeight = targetWeightInput.text.toString().toIntOrNull()
            var hasErrors = false // Переменная для отслеживания ошибок

            // Проверка на корректность роста
            if (height == null || height < resources.getInteger(R.integer.min_height)
                || height > resources.getInteger(R.integer.max_height)) {
                Toast.makeText(context, "Введите корректный рост (от 130 до 220 см)", Toast.LENGTH_SHORT).show()
                hasErrors = true
            }

            // Проверка на корректность веса
            if (targetWeight == null || targetWeight < resources.getInteger(R.integer.min_weight)
                || targetWeight > resources.getInteger(R.integer.max_weight)) {
                Toast.makeText(context, "Введите корректный целевой вес (от 40 до 150 кг)", Toast.LENGTH_SHORT).show()
                hasErrors = true
            }

            if (!isValidDate(birthInput.text.toString())) {
                Toast.makeText(context, "Введите корректную дату рождения", Toast.LENGTH_SHORT).show()
                hasErrors = true
            }

            if (!hasErrors) {
                // Обновляем данные, если ошибок нет
                view?.findViewById<TextView>(R.id.userName)?.text = nameInput.text.toString()
                view?.findViewById<TextView>(R.id.heightText)?.text = "Рост: ${height} см"
                view?.findViewById<TextView>(R.id.birthdateText)?.text = "Дата рождения: ${birthInput.text}"
                view?.findViewById<TextView>(R.id.targetWeightText)?.text = "Целевой вес: ${targetWeight} кг"
                view?.findViewById<TextView>(R.id.activityLevelText)?.text = "Уровень активности: ${activityPicker.text}"
                view?.findViewById<TextView>(R.id.trainingGoalText)?.text = "Цель: ${goalPicker.text}"

                try {
                    val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
                    val accessToken = preferences.getString("access_jwt", null)

                    updateData(accessToken.toString(),
                        nameInput.text.toString(),
                        height.toString(),
                        birthInput.text.toString(),
                        targetWeight.toString(),
                        activityPicker.text.toString(),
                        goalPicker.text.toString())
                    dialog.dismiss() // Закрываем диалог только при успешном сохранении
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), "Ошибка сохранение данных", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }


    private fun showChoiceDialog(title: String, items: Array<String>, onItemSelected: (String) -> Unit) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_bottom_list, null)
        val titleView = view.findViewById<TextView>(R.id.dialogTitle)
        val recyclerView = view.findViewById<RecyclerView>(R.id.iosList)

        titleView.text = title
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = UserDataPickerAdapter(items.toList()) {
            onItemSelected(it)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun logoutAccount() {
        // Получаем SharedPreferences
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)

        // Специальный редактор для удаления данных
        val editor = preferences.edit()
        editor.clear() // Удаляем все данные
        editor.apply() // Применяем изменения

        // Выводим сообщение о выходе
        Toast.makeText(requireActivity(), "Вы успешно вышли из аккаунта", Toast.LENGTH_SHORT).show()

        // Переход на MainActivity
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)

        // Закрываем текущую активность, если нужно
        requireActivity().finish()
    }



    private fun isValidDate(date: String): Boolean {
        // Проверка формата
        if (!Regex("""\d{2}\.\d{2}\.\d{4}""").matches(date)) {
            return false
        }

        val day = date.substring(0, 2).toInt()
        val month = date.substring(3, 5).toInt()
        val year = date.substring(6, 10).toInt()


        // Проверка на валидность дня, месяца и года
        if (day !in 1..31 || month !in 1..12 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            return false
        }


        // Проверка существования даты
        return try {
            val dateFormat = SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())
            dateFormat.isLenient = false
            val parsedDate = dateFormat.parse(date)
            val currentDate = Calendar.getInstance().time
            parsedDate != null && !parsedDate.after(currentDate)
        } catch (e: Exception) {
            false
        }
    }
}

