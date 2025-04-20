package com.example.vkr_pulse

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.logoutButton).setOnClickListener {
            Toast.makeText(requireContext(), "Выход из аккаунта", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.editWeightButton).setOnClickListener {
            showEditWeightDialog()
        }

        view.findViewById<Button>(R.id.editPersonalDataButton).setOnClickListener {
            showEditUserDataDialog()
        }
    }

    private fun showEditWeightDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_weight, null)
        val editText = dialogView.findViewById<EditText>(R.id.weightInput)
        val saveButton = dialogView.findViewById<Button>(R.id.saveWeightButton)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(dialogView)
            .create()

        saveButton.setOnClickListener {
            val weightStr = editText.text.toString()
            if (weightStr.isNotBlank()) {
                val weight = weightStr.toFloatOrNull()
                if (weight != null) {
                    view?.findViewById<TextView>(R.id.weightText)?.text = "Текущий вес: $weight кг"
                    dialog.dismiss()
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

        heightInput.setText(
            view?.findViewById<TextView>(R.id.heightText)?.text?.toString()?.filter { it.isDigit() }
        )

        birthInput.setText(
            view?.findViewById<TextView>(R.id.birthdateText)?.text?.toString()
                ?.substringAfter(": ")?.trim()
        )

        targetWeightInput.setText(
            view?.findViewById<TextView>(R.id.targetWeightText)?.text?.toString()?.filter { it.isDigit() }
        )

        activityPicker.text = view?.findViewById<TextView>(R.id.activityLevelText)
            ?.text?.toString()?.substringAfter(": ")?.trim()

        goalPicker.text = view?.findViewById<TextView>(R.id.trainingGoalText)
            ?.text?.toString()?.substringAfter(": ")?.trim()

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
            view?.findViewById<TextView>(R.id.userName)?.text = nameInput.text.toString()
            view?.findViewById<TextView>(R.id.heightText)?.text = "Рост: ${heightInput.text} см"
            view?.findViewById<TextView>(R.id.birthdateText)?.text = "Дата рождения: ${birthInput.text}"
            view?.findViewById<TextView>(R.id.targetWeightText)?.text = "Целевой вес: ${targetWeightInput.text} кг"
            view?.findViewById<TextView>(R.id.activityLevelText)?.text = "Уровень активности: ${activityPicker.text}"
            view?.findViewById<TextView>(R.id.trainingGoalText)?.text = "Цель: ${goalPicker.text}"
            dialog.dismiss()
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

}
