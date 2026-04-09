package com.example.mobdev_lab3.ui.storage

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.R
import com.example.mobdev_lab3.adapter.StorageAdapter
import com.example.mobdev_lab3.model.Note
import com.example.mobdev_lab3.repository.NoteStorage
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

/**
 * Базовый абстрактный фрагмент для всех типов хранилищ
 * Содержит общую логику для работы с заметками
 */
abstract class BaseStorageFragment : Fragment() {

    protected abstract val storage: NoteStorage

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StorageAdapter
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var storageTypeText: TextView
    private lateinit var storagePathText: TextView
    private lateinit var openFileButton: Button

    private val notes = mutableListOf<Note>()

    /**
     * Абстрактный метод для получения кнопки "Открыть файл"
     * Переопределяется в подклассах для специфичной функциональности
     */
    protected open fun setupOpenFileButton() {
        openFileButton.visibility = View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_storage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация views
        recyclerView = view.findViewById(R.id.notesRecyclerView)
        fabAddNote = view.findViewById(R.id.fabAddNote)
        storageTypeText = view.findViewById(R.id.storageTypeText)
        storagePathText = view.findViewById(R.id.storagePathText)
        openFileButton = view.findViewById(R.id.openFileButton)

        setupRecyclerView()
        setupOpenFileButton()
        updateStorageInfo()
        loadNotes()

        fabAddNote.setOnClickListener {
            showAddNoteDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = StorageAdapter(
            onEditClick = { note -> showEditNoteDialog(note) },
            onDeleteClick = { note -> deleteNote(note) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    protected fun loadNotes() {
        lifecycleScope.launch {
            storage.getAll().fold(
                onSuccess = { loadedNotes ->
                    notes.clear()
                    notes.addAll(loadedNotes)
                    adapter.updateNotes(notes)
                },
                onFailure = { error ->
                    Toast.makeText(
                        requireContext(),
                        "Ошибка загрузки: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun updateStorageInfo() {
        val info = storage.getStorageInfo()
        val lines = info.split("\n")
        if (lines.size >= 2) {
            storageTypeText.text = lines[0]
            storagePathText.text = lines[1]
        }
    }

    protected fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_note, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val contentEditText = dialogView.findViewById<EditText>(R.id.contentEditText)

        dialogTitle.text = "Добавить заметку"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.saveButton).setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty()) {
                titleEditText.error = "Введите заголовок"
                return@setOnClickListener
            }

            val note = Note(title = title, content = content)
            lifecycleScope.launch {
                storage.create(note).fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Заметка создана", Toast.LENGTH_SHORT).show()
                        loadNotes()
                        dialog.dismiss()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        dialog.show()
    }

    protected fun showEditNoteDialog(note: Note) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_note, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
        val contentEditText = dialogView.findViewById<EditText>(R.id.contentEditText)

        dialogTitle.text = "Редактировать заметку"
        titleEditText.setText(note.title)
        contentEditText.setText(note.content)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.cancelButton).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.saveButton).setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()

            if (title.isEmpty()) {
                titleEditText.error = "Введите заголовок"
                return@setOnClickListener
            }

            val updatedNote = note.copy(title = title, content = content, timestamp = System.currentTimeMillis())
            lifecycleScope.launch {
                storage.update(updatedNote).fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Заметка обновлена", Toast.LENGTH_SHORT).show()
                        loadNotes()
                        dialog.dismiss()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            requireContext(),
                            "Ошибка: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }

        dialog.show()
    }

    protected fun deleteNote(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить заметку?")
            .setMessage("Вы уверены, что хотите удалить \"${note.title}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    storage.delete(note.id).fold(
                        onSuccess = {
                            Toast.makeText(requireContext(), "Заметка удалена", Toast.LENGTH_SHORT).show()
                            loadNotes()
                        },
                        onFailure = { error ->
                            Toast.makeText(
                                requireContext(),
                                "Ошибка: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
