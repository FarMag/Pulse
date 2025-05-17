package com.example.vkr_pulse.ui.dialogs

//import android.app.Dialog
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.LayoutInflater
//import android.widget.Button
//import android.widget.EditText
//import android.widget.Toast
//import androidx.appcompat.app.AlertDialog
//import androidx.fragment.app.DialogFragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.vkr_pulse.R
//import com.example.vkr_pulse.data.FoodItem
//import com.example.vkr_pulse.data.ProductDatabaseHelper
//import com.example.vkr_pulse.ui.adapters.ProductAdapter
//
//class AddProductDialogFragment(
//    private val onProductAdded: (FoodItem, Int, String) -> Unit,
//    private val mealType: String
//) : DialogFragment() {
//
//    private lateinit var searchEditText: EditText
//    private lateinit var productsRecyclerView: RecyclerView
//    private lateinit var gramsEditText: EditText
//    private lateinit var confirmButton: Button
//    private lateinit var adapter: ProductAdapter
//
//    private var selectedProduct: FoodItem? = null
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val builder = AlertDialog.Builder(requireContext())
//        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_product, null)
//        builder.setView(view)
//
//        searchEditText = view.findViewById(R.id.searchInput)
//        productsRecyclerView = view.findViewById(R.id.productList)
//        gramsEditText = view.findViewById(R.id.gramsInput)
//        confirmButton = view.findViewById(R.id.addProductButton)
//
//        val dbHelper = ProductDatabaseHelper(requireContext())
//        val initialList = dbHelper.searchProducts("").map {
//            FoodItem(it.id, it.name, it.calories, it.protein, it.fats, it.carbs)
//        }
//
//        adapter = ProductAdapter(initialList) { product ->
//            selectedProduct = product
//            adapter.setSelectedProduct(product) // для подсветки
//        }
//
//        productsRecyclerView.layoutManager = LinearLayoutManager(context)
//        productsRecyclerView.adapter = adapter
//
//        searchEditText.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable?) {
//                val query = s.toString()
//                val results = dbHelper.searchProducts(query).map {
//                    FoodItem(it.id, it.name, it.calories, it.protein, it.fats, it.carbs)
//                }
//                adapter.updateData(results)
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
//        })
//
//        confirmButton.setOnClickListener {
//            val grams = gramsEditText.text.toString().toIntOrNull()
//            val product = selectedProduct
//
//            if (product != null && grams != null && grams > 0) {
//                onProductAdded(product, grams, mealType)
//                dismiss()
//            } else {
//                Toast.makeText(requireContext(), "Выберите продукт и введите граммы", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        return builder.create().apply {
//            window?.setBackgroundDrawableResource(android.R.color.transparent)
//        }
//    }
//
//    companion object {
//        fun newInstance(mealType: String, onProductAdded: (FoodItem, Int, String) -> Unit): AddProductDialogFragment {
//            return AddProductDialogFragment(onProductAdded, mealType)
//        }
//    }
//}
















import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr_pulse.R
import com.example.vkr_pulse.data.FoodItem
import com.example.vkr_pulse.ui.adapters.ProductAdapter
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class AddProductDialogFragment(
    private val onProductAdded: (FoodItem, Int, String) -> Unit,
    private val mealType: String
) : DialogFragment() {

    private lateinit var searchEditText: EditText
    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var gramsEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var adapter: ProductAdapter

    private var selectedProduct: FoodItem? = null
    private val client = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_product, null)
        builder.setView(view)

        searchEditText = view.findViewById(R.id.searchInput)
        productsRecyclerView = view.findViewById(R.id.productList)
        gramsEditText = view.findViewById(R.id.gramsInput)
        confirmButton = view.findViewById(R.id.addProductButton)

        adapter = ProductAdapter(emptyList()) { product ->
            selectedProduct = product
            adapter.setSelectedProduct(product) // для подсветки
        }

        productsRecyclerView.layoutManager = LinearLayoutManager(context)
        productsRecyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Убираем предыдущий runnable, если он существует
                searchRunnable?.let { handler.removeCallbacks(it) }
                // Создаём новый runnable, который будет выполнен через 2 секунды
                searchRunnable = Runnable {
                    val query = s.toString()
                    fetchProducts(query) // Запрос на сервер с поиском
                }
                handler.postDelayed(searchRunnable!!, 1000) // 1 секунды задержки
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmButton.setOnClickListener {
            val grams = gramsEditText.text.toString().toIntOrNull()
            val product = selectedProduct

            if (product != null && grams != null && grams > 0) {
                onProductAdded(product, grams, mealType)

                addProductUser(product, grams, mealType)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Выберите продукт и введите граммы", Toast.LENGTH_SHORT).show()
            }
        }

        return builder.create().apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun fetchProducts(query: String) {
        val url_nutrition = getString(R.string.url_recipes) + "showProductData"
        val requestBody = FormBody.Builder()
            .add("search_name_product", query)
            .build()

        val request = Request.Builder()
            .url(url_nutrition)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)
                        val productList = mutableListOf<FoodItem>()

                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val id = jsonObject.getInt("id")
                            val name = jsonObject.getString("name")
                            val calories = jsonObject.getDouble("calories")
                            val protein = jsonObject.getDouble("protein")
                            val fats = jsonObject.getDouble("fat")
                            val carbs = jsonObject.getDouble("carbohydrates")

                            productList.add(FoodItem(id, name, calories.toInt(), protein.toFloat(), fats.toFloat(), carbs.toFloat()))
                        }

                        requireActivity().runOnUiThread {
                            adapter.updateData(productList)
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun addProductUser(product: FoodItem, grams: Int, mealType: String) {
        val preferences = requireActivity().getSharedPreferences("myPrefs", AppCompatActivity.MODE_PRIVATE)
        val accessToken = preferences.getString("access_jwt", null)

        val convertedMealType = when (mealType) {
            "Завтрак" -> "breakfast"
            "Обед" -> "lunch"
            "Ужин" -> "dinner"
            "Перекусы" -> "snack"
            else -> mealType // если значение не совпадает, оставляем как есть
        }

        val url_nutrition = getString(R.string.url_nutrition) + "addProductUser"



        // Создание тела запроса с добавлением данных о продукте и пользователе
        val requestBody = FormBody.Builder()
            .add("access_token", accessToken.toString())
            .add("product_id", product.id.toString())
            .add("product_name", product.name)
            .add("grams", grams.toString())
            .add("meal_type", convertedMealType)
            .add("calories", (product.calories * grams / 100).toString())
            .add("protein", (product.protein * grams / 100).toInt().toString()) // Общий протеин
            .add("fats", (product.fats * grams / 100).toInt().toString()) // Общий жир
            .add("carbs", (product.carbs * grams / 100).toInt().toString()) // Общие углеводы
            .build()

        val request = Request.Builder()
            .url(url_nutrition)
            .post(requestBody)
            .build()

        // Отправка запроса в асинхронном потоке
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace() // Обработка ошибок
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Успех! Можно обработать ответ
                    val responseBody = response.body?.string()
                    // Обработка полученных данных
                } else {
                    // Ошибка сервера
                }
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем handler от всех выполненных задач при закрытии диалога
        searchRunnable?.let { handler.removeCallbacks(it) }
    }

    companion object {
        fun newInstance(mealType: String, onProductAdded: (FoodItem, Int, String) -> Unit): AddProductDialogFragment {
            return AddProductDialogFragment(onProductAdded, mealType)
        }
    }
}


