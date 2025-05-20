package com.example.vkr_pulse

import android.app.Dialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Scroller
import androidx.fragment.app.DialogFragment

class NotesDialogFragment(
    private val initialText: String = "",
    private val onSave: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_notes, null)

        val notesEditText = view.findViewById<EditText>(R.id.notesEditText)
        val saveButton = view.findViewById<Button>(R.id.saveNotesButton)
        val cancelButton = view.findViewById<Button>(R.id.cancelNotesButton)


        notesEditText.setText(initialText)
        notesEditText.setSelection(notesEditText.text.length)

        // Enable scrolling in EditText
        notesEditText.setScroller(Scroller(requireContext()))
        notesEditText.isVerticalScrollBarEnabled = true
        notesEditText.movementMethod = ScrollingMovementMethod()
        notesEditText.isSingleLine = false
        notesEditText.maxLines = 7 // или столько, сколько нужно

        // Optional: Фокус и автоматическое открытие клавиатуры
        notesEditText.requestFocus()

        saveButton.setOnClickListener {
            val text = notesEditText.text.toString()
            onSave(text)
            dismiss()
        }
        cancelButton.setOnClickListener { dismiss() }

        val dialog = Dialog(requireContext())
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        // Optional: открыть клавиатуру автоматически
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }
}
