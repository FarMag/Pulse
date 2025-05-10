package com.example.vkr_pulse.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vkr_pulse.R
import com.example.vkr_pulse.data.FoodItem

class ProductAdapter(
    private var products: List<FoodItem>,
    private val onClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private var selectedProduct: FoodItem? = null

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val kcalInfo: TextView = view.findViewById(R.id.kcalInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_search, parent, false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productName.text = product.name
        holder.kcalInfo.text = "${product.calories} ккал, Б: ${product.protein} / Ж: ${product.fats} / У: ${product.carbs}"

        val isSelected = product == selectedProduct
        holder.itemView.setBackgroundResource(
            if (isSelected) R.drawable.selected_product_background else android.R.color.transparent
        )

        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousSelected = selectedPosition
            selectedPosition = currentPos

            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)

            onClick(product)
        }
    }

    fun updateData(newProducts: List<FoodItem>) {
        products = newProducts
        selectedPosition = RecyclerView.NO_POSITION
        notifyDataSetChanged()
    }

    fun setSelectedProduct(product: FoodItem) {
        selectedProduct = product
        notifyDataSetChanged()
    }
}
