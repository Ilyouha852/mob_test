package com.example.mobdev_lab3.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.model.BookmarkColor

class TagManagementDialog(private val context: Context) {

    fun showCreateTagDialog(onTagCreated: (String) -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Создать тег")

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_create_tag, null)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerTagColor)

        // Setup type spinner (using BookmarkColor display names as tag types)
        val tagTypes = BookmarkColor.values()
        val tagTypeNames = tagTypes.map { it.displayName }
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, tagTypeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        builder.setView(view)

        builder.setPositiveButton("Создать") { _, _ ->
            val name = tagTypeNames[spinnerType.selectedItemPosition]
            if (name.isNotEmpty()) {
                onTagCreated(name)
            }
        }
        builder.setNegativeButton("Отмена") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    fun showAssignTagsDialog(
        context: Context,
        allTags: List<Tag>,
        currentTags: List<Tag>,
        onTagsSelected: (List<Tag>) -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Назначить теги")

        val tagNames = allTags.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(allTags.size) { index ->
            currentTags.any { it.id == allTags[index].id }
        }
        val selectedTags = ArrayList<Tag>(currentTags)

        builder.setMultiChoiceItems(tagNames, checkedItems) { _, which, isChecked ->
            val tag = allTags[which]
            if (isChecked) {
                selectedTags.add(tag)
            } else {
                selectedTags.removeAll { it.id == tag.id }
            }
        }

        builder.setPositiveButton("OK") { _, _ ->
            onTagsSelected(selectedTags)
        }
        builder.setNegativeButton("Отмена", null)

        builder.show()
    }
}
