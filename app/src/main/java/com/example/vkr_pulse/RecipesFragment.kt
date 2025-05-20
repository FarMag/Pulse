package com.example.vkr_pulse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.vkr_pulse.data.ProductDatabaseHelper
import okhttp3.FormBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class RecipesFragment : Fragment() {

    private var userGoal: String = "" // Инициализируем переменную с пустой строкой
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)
//        val userGoal = "" // "mass", "keeping", "losing"

        if (accessToken != null) {
            getUserGoal(accessToken)
        }

        // Кнопка закрытия
        view.findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun getUserGoal(accessToken: String) {
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
                // Можно показать ошибку через Toast на UI-потоке
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()
                if (response.isSuccessful && data != null) {
                    requireActivity().runOnUiThread {
                        try {
                            // Парсим JSON
                            val jsonResponse = JSONObject(data)
                            userGoal = jsonResponse.getString("target_phis")
                            loadRecipes(userGoal ?: "") // Передаем пустую строку, если userGoal равен null
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            // Обработка ошибки парсинга
                            Toast.makeText(requireContext(), "Ошибка парсинга данных", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Обработка ошибки
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Ошибка ответа от сервера: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }



    private fun loadRecipes(goal: String) {
        val client = OkHttpClient()

        val url = getString(R.string.url_recipes) + "showProductDataGoal"

        val formBody = FormBody.Builder()
//            .add("access_token", accessToken)
            .add("goal", goal) // например, "mass", "keeping", "losing"
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                // Можно показать ошибку через Toast на UI-потоке
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string()
                if (response.isSuccessful && data != null) {
                    requireActivity().runOnUiThread {
                        // Здесь парсим JSON и отображаем в твоём LinearLayout или RecyclerView
                        // Например, вызываем функцию:
                        showRecipes(data)
                    }
                } else {
                    // Обработка ошибки
                }
            }
        })
    }


    private fun showRecipes(data: String) {
        val recipesContainer = view?.findViewById<LinearLayout>(R.id.recipesContainer) ?: return
        recipesContainer.removeAllViews()

        val recipesArray = JSONArray(data)
        for (i in 0 until recipesArray.length()) {
            val recipeObj = recipesArray.getJSONObject(i)
            val id = recipeObj.getString("id")
            val name = recipeObj.getString("name")
            val calories = recipeObj.getInt("calories")
            val protein = recipeObj.getDouble("protein")
            val fat = recipeObj.getDouble("fat")
            val carbs = recipeObj.getDouble("carbohydrates")
            val description = recipeObj.optString("description", "") // если нет, используй "ingredient_name"
            val ingredientList = recipeObj.optString("ingredient_name", "")
            // Например, ingredientList это твой "рецепт" - если нет отдельного поля

            val itemView = layoutInflater.inflate(R.layout.item_recipe, recipesContainer, false)
            itemView.findViewById<TextView>(R.id.recipeName).text = name
            itemView.findViewById<TextView>(R.id.recipeInfo).text =
                "Ккал: $calories | Б: ${"%.1f".format(protein)} | Ж: ${"%.1f".format(fat)} | У: ${"%.1f".format(carbs)}"

            // ФОТО блюда
            val imageView = itemView.findViewById<ImageView>(R.id.recipeImage)
            val imageName = "food_$id"
            val resId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
            imageView.setImageResource(if (resId != 0) resId else R.drawable.food_default)

            // --- КЛИК: ОТКРЫТЬ МОДАЛКУ ---
            itemView.setOnClickListener {
                showRecipeDialog(
                    id = id,
                    name = name,
                    calories = calories,
                    protein = protein,
                    fat = fat,
                    carbs = carbs,
                    description = description,
                    recipe = ingredientList
                )
            }

            recipesContainer.addView(itemView)
        }
    }

    private fun showRecipeDialog(
        id: String,
        name: String,
        calories: Int,
        protein: Double,
        fat: Double,
        carbs: Double,
        description: String,
        recipe: String
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recipe_details, null)

        // Фото-полоса сверху
        val imageView = dialogView.findViewById<ImageView>(R.id.recipeDialogImage)
        val imageName = "food_$id"
        val resId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
        imageView.setImageResource(if (resId != 0) resId else R.drawable.food_default)

        dialogView.findViewById<TextView>(R.id.recipeDialogName).text = name
        dialogView.findViewById<TextView>(R.id.recipeDialogKcal).text =
            "Ккал: $calories | Б: ${"%.1f".format(protein)} | Ж: ${"%.1f".format(fat)} | У: ${"%.1f".format(carbs)}"

        dialogView.findViewById<TextView>(R.id.recipeDialogRecipe).text =
            if (recipe.isNotBlank()) "$recipe" else "Рецепт не указан"

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.closeDialogButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }



}
