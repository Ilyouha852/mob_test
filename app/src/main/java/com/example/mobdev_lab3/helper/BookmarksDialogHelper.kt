package com.example.mobdev_lab3.helper

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.manager.BookmarksManager
import com.example.mobdev_lab3.model.BookmarkColor
import com.example.mobdev_lab3.model.FileBookmark
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class BookmarksDialogHelper(private val context: Context) {

    fun showAddBookmarkDialog(
        initialPath: String? = null,
        initialIsDir: Boolean = true,
        initialName: String? = null,
        initialColor: BookmarkColor? = null,
        onBookmarkAdded: () -> Unit
    ) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_bookmark, null)
        
        val editTextName = dialogView.findViewById<TextInputEditText>(R.id.editTextName)
        val textViewPath = dialogView.findViewById<TextView>(R.id.textViewPath)
        val buttonSelectPath = dialogView.findViewById<Button>(R.id.buttonSelectPath)
        val editTextDescription = dialogView.findViewById<TextInputEditText>(R.id.editTextDescription)
        val spinnerColor = dialogView.findViewById<Spinner>(R.id.spinnerColor)
        val textViewTitle = dialogView.findViewById<TextView>(R.id.textViewTitle)

        textViewTitle.text = "Добавить закладку"
        var selectedPath = initialPath
        var selectedIsDirectory = initialIsDir

        if (initialPath != null) {
            textViewPath.text = initialPath
        }
        
        if (initialName != null) {
            editTextName.setText(initialName)
        }

        setupColorSpinner(spinnerColor)
        if (initialColor != null) {
            spinnerColor.setSelection(initialColor.ordinal)
        }

        buttonSelectPath.setOnClickListener {
            showPathInputDialog { path, isDir ->
                selectedPath = path
                selectedIsDirectory = isDir
                textViewPath.text = path
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            val name = editTextName.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val colorPosition = spinnerColor.selectedItemPosition

            if (name.isEmpty()) {
                Toast.makeText(context, "Введите название закладки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedPath == null) {
                Toast.makeText(context, "Выберите путь", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bookmark = FileBookmark(
                id = FileBookmark.getNextId(),
                name = name,
                path = selectedPath!!,
                description = description,
                color = BookmarkColor.values()[colorPosition],
                createdDate = System.currentTimeMillis(),
                isDirectory = selectedIsDirectory
            )

            BookmarksManager.addBookmark(context, bookmark)
            Toast.makeText(context, "Закладка добавлена", Toast.LENGTH_SHORT).show()
            onBookmarkAdded()
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupColorSpinner(spinner: Spinner) {
        val colors = BookmarkColor.values().map { it.displayName }
        val arrayAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, colors)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter
    }

    private fun showPathInputDialog(onPathSelected: (String, Boolean) -> Unit) {
        val editText = EditText(context)
        editText.hint = "Введите путь (например, /storage/emulated/0/Download)"
        editText.setText("/storage/emulated/0/")

        AlertDialog.Builder(context)
            .setTitle("Выбор пути")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val path = editText.text.toString().trim()
                if (path.isNotEmpty()) {
                    val file = File(path)
                    onPathSelected(path, file.isDirectory || path.endsWith("/"))
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
