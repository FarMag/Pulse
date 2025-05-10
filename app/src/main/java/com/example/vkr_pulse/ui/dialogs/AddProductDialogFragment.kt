package com.example.vkr_pulse.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr_pulse.R
import com.example.vkr_pulse.data.FoodItem
import com.example.vkr_pulse.data.ProductDatabaseHelper
import com.example.vkr_pulse.ui.adapters.ProductAdapter

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_product, null)
        builder.setView(view)

        searchEditText = view.findViewById(R.id.searchInput)
        productsRecyclerView = view.findViewById(R.id.productList)
        gramsEditText = view.findViewById(R.id.gramsInput)
        confirmButton = view.findViewById(R.id.addProductButton)

        val dbHelper = ProductDatabaseHelper(requireContext())
        val initialList = dbHelper.searchProducts("").map {
            FoodItem(it.id, it.name, it.calories, it.protein, it.fats, it.carbs)
        }

        adapter = ProductAdapter(initialList) { product ->
            selectedProduct = product
            adapter.setSelectedProduct(product) // для подсветки
        }

        productsRecyclerView.layoutManager = LinearLayoutManager(context)
        productsRecyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val results = dbHelper.searchProducts(query).map {
                    FoodItem(it.id, it.name, it.calories, it.protein, it.fats, it.carbs)
                }
                adapter.updateData(results)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmButton.setOnClickListener {
            val grams = gramsEditText.text.toString().toIntOrNull()
            val product = selectedProduct

            if (product != null && grams != null && grams > 0) {
                onProductAdded(product, grams, mealType)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Выберите продукт и введите граммы", Toast.LENGTH_SHORT).show()
            }
        }

        return builder.create().apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    companion object {
        fun newInstance(mealType: String, onProductAdded: (FoodItem, Int, String) -> Unit): AddProductDialogFragment {
            return AddProductDialogFragment(onProductAdded, mealType)
        }
    }
}
