package com.example.vkr_pulse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

//    private fun showRecipes(data: String) {
//        val recipesContainer = view?.findViewById<LinearLayout>(R.id.recipesContainer) ?: return
//
//        // Очищаем старые блоки
//        recipesContainer.removeAllViews()
//
//        // Парсим массив JSON
//        val recipesArray = JSONArray(data)
//        for (i in 0 until recipesArray.length()) {
//            val recipeObj = recipesArray.getJSONObject(i)
//
//            val id = recipeObj.getString("id")
//            val name = recipeObj.getString("name")
//            val calories = recipeObj.getInt("calories")
//            val protein = recipeObj.getDouble("protein")
//            val fat = recipeObj.getDouble("fat")
//            val carbs = recipeObj.getDouble("carbohydrates")
//            val description = recipeObj.getString("ingredient_name")
//
//            // Создай блок для рецепта (например, через item_recipe.xml)
//            val itemView = layoutInflater.inflate(R.layout.item_recipe, recipesContainer, false)
//
//            // Пример: наполнение itemView
//            itemView.findViewById<TextView>(R.id.recipeName).text = name
//            itemView.findViewById<TextView>(R.id.recipeInfo).text = "Ккал: $calories | Б: ${"%.1f".format(protein)} | Ж: ${"%.1f".format(fat)} | У: ${"%.1f".format(carbs)}"
//
//
//            // itemView.findViewById<ImageView>(R.id.recipeImage).setImageResource(R.drawable.food_placeholder)
//
//            // Добавляем itemView в контейнер
//            recipesContainer.addView(itemView)
//        }
//    }

//    private fun showRecipes(data: String) {
//        val recipesContainer = view?.findViewById<LinearLayout>(R.id.recipesContainer) ?: return
//        recipesContainer.removeAllViews()
//
//        val recipesArray = JSONArray(data)
//        for (i in 0 until recipesArray.length()) {
//            val recipeObj = recipesArray.getJSONObject(i)
//
//            val name = recipeObj.getString("name")
//            val calories = recipeObj.getInt("calories")
//            val protein = recipeObj.getDouble("protein")
//            val fat = recipeObj.getDouble("fat")
//            val carbs = recipeObj.getDouble("carbohydrates")
//            val imageUrl = recipeObj.optString("image_url", null)
//
//            val itemView = layoutInflater.inflate(R.layout.item_recipe, recipesContainer, false)
//
//            itemView.findViewById<TextView>(R.id.recipeName).text = name
//            itemView.findViewById<TextView>(R.id.recipeInfo).text =
//                "Ккал: $calories | Б: ${"%.1f".format(protein)} | Ж: ${"%.1f".format(fat)} | У: ${"%.1f".format(carbs)}"
//
//            val imageView = itemView.findViewById<ImageView>(R.id.recipeImage)
//
//            if (!imageUrl.isNullOrEmpty()) {
//                Glide.with(this)
//                    .load(imageUrl)
//                    .placeholder(R.drawable.food)
//                    .error(R.drawable.food) // в случае ошибки
//                    .into(imageView)
//            } else {
//                imageView.setImageResource(R.drawable.food)
//            }
//
//            recipesContainer.addView(itemView)
//        }
//    }

//    private fun showRecipes(data: String) {
//        val recipesContainer = view?.findViewById<LinearLayout>(R.id.recipesContainer) ?: return
//        recipesContainer.removeAllViews()
//
//        val recipesArray = JSONArray(data)
//        for (i in 0 until recipesArray.length()) {
//            val recipeObj = recipesArray.getJSONObject(i)
//            val id = recipeObj.getString("id") // Например, "827"
//            val name = recipeObj.getString("name")
//            val calories = recipeObj.getInt("calories")
//            val protein = recipeObj.getDouble("protein")
//            val fat = recipeObj.getDouble("fat")
//            val carbs = recipeObj.getDouble("carbohydrates")
//
//            val itemView = layoutInflater.inflate(R.layout.item_recipe, recipesContainer, false)
//
//            itemView.findViewById<TextView>(R.id.recipeName).text = name
//            itemView.findViewById<TextView>(R.id.recipeInfo).text =
//                "Ккал: $calories | Б: ${"%.1f".format(protein)} | Ж: ${"%.1f".format(fat)} | У: ${"%.1f".format(carbs)}"
//
//            val imageView = itemView.findViewById<ImageView>(R.id.recipeImage)
//
//            // Получаем идентификатор ресурса изображения по имени, например "img827"
//            val imageName = "img$id"
//            val resId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
//
//            if (resId != 0) {
//                imageView.setImageResource("food_".resId)
//            } else {
//                imageView.setImageResource(R.drawable.food) // Заглушка, если изображение не найдено
////                imageView.setImageResource(R.drawable.food_820)
//            }
//
//            recipesContainer.addView(itemView)
//        }
//    }

    private fun showRecipes(data: String) {
        val recipesContainer = view?.findViewById<LinearLayout>(R.id.recipesContainer) ?: return
        recipesContainer.removeAllViews()

        val recipesArray = JSONArray(data)
        for (i in 0 until recipesArray.length()) {
            val recipeObj = recipesArray.getJSONObject(i)
            val id = recipeObj.getString("id") // Например, "827"
            val name = recipeObj.getString("name")
            val calories = recipeObj.getInt("calories")
            val protein = recipeObj.getDouble("protein")
            val fat = recipeObj.getDouble("fat")
            val carbs = recipeObj.getDouble("carbohydrates")

            val itemView = layoutInflater.inflate(R.layout.item_recipe, recipesContainer, false)

            itemView.findViewById<TextView>(R.id.recipeName).text = name
            itemView.findViewById<TextView>(R.id.recipeInfo).text =
                "Ккал: $calories | Б: ${"%.1f".format(protein)} | Ж: ${"%.1f".format(fat)} | У: ${"%.1f".format(carbs)}"

            val imageView = itemView.findViewById<ImageView>(R.id.recipeImage)

            // Получаем идентификатор ресурса изображения по имени, например "img827"
            val imageName = "food_$id"
            val resId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)

            if (resId != 0) {
                imageView.setImageResource(resId) // Исправлено
            } else {
                imageView.setImageResource(R.drawable.food_default) // Заглушка, если изображение не найдено
            }

            recipesContainer.addView(itemView)
        }
    }


}
