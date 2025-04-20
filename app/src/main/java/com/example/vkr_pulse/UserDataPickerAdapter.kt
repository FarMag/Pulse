package com.example.vkr_pulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserDataPickerAdapter(
    private val options: List<String>,
    private val onSelect: (String) -> Unit
) : RecyclerView.Adapter<UserDataPickerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val optionText: TextView = view.findViewById(R.id.itemText)

        init {
            view.setOnClickListener {
                onSelect(options[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_picker_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = options.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.optionText.text = options[position]
    }
}
