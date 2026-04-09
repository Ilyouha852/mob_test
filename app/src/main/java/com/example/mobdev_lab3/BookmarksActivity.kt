package com.example.mobdev_lab3

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobdev_lab3.adapter.BookmarkAdapter
import com.example.mobdev_lab3.model.BookmarkColor
import com.example.mobdev_lab3.model.FileBookmark
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.mobdev_lab3.helper.BookmarksDialogHelper
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BookmarksActivity : AppCompatActivity() {

    private lateinit var recyclerViewBookmarks: RecyclerView
    private lateinit var fabAddBookmark: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var adapter: BookmarkAdapter
    
    private val bookmarks = mutableListOf<FileBookmark>()
    private var selectedPath: String? = null
    private var selectedIsDirectory: Boolean = true

    companion object {
        private const val PREFS_NAME = "bookmarks_prefs"
        private const val KEY_BOOKMARKS = "bookmarks_data"
        const val EXTRA_ADD_BOOKMARK_PATH = "add_bookmark_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        // Загрузка сохраненных закладок
        loadBookmarks()

        // Инициализация представлений
        toolbar = findViewById(R.id.toolbar)
        recyclerViewBookmarks = findViewById(R.id.recyclerViewBookmarks)
        fabAddBookmark = findViewById(R.id.fabAddBookmark)

        // Настройка панели инструментов
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Закладки"

        // Настройка RecyclerView с GridLayoutManager (3 колонки)
        val layoutManager = GridLayoutManager(this, 3)
        recyclerViewBookmarks.layoutManager = layoutManager

        // Настройка адаптера
        adapter = BookmarkAdapter(
            bookmarks = bookmarks,
            onItemClick = { bookmark ->
                openBookmark(bookmark)
            },
            onDeleteClick = { bookmark ->
                showDeleteConfirmation(bookmark)
            },
            onEditClick = { bookmark ->
                showEditBookmarkDialog(bookmark)
            }
        )
        recyclerViewBookmarks.adapter = adapter

        // Настройка FAB
        fabAddBookmark.setOnClickListener {
            BookmarksDialogHelper(this).showAddBookmarkDialog {
                loadBookmarks()
                adapter.updateBookmarks(bookmarks)
            }
        }

        // Проверка на добавление закладки из другого окна
        val addPath = intent.getStringExtra(EXTRA_ADD_BOOKMARK_PATH)
        if (addPath != null) {
            val file = File(addPath)
            BookmarksDialogHelper(this).showAddBookmarkDialog(addPath, file.isDirectory) {
                loadBookmarks()
                adapter.updateBookmarks(bookmarks)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openBookmark(bookmark: FileBookmark) {
        val file = File(bookmark.path)
        val intent = Intent(this, MainActivity::class.java)
        
        if (bookmark.isDirectory) {
            // Переход в саму директорию
            intent.putExtra(MainActivity.EXTRA_CURRENT_PATH, bookmark.path)
        } else {
            // Для файлов переходим в родительскую директорию
            val parentPath = file.parent ?: bookmark.path
            intent.putExtra(MainActivity.EXTRA_CURRENT_PATH, parentPath)
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }



    private fun showEditBookmarkDialog(bookmark: FileBookmark) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_bookmark, null)
        
        val editTextName = dialogView.findViewById<TextInputEditText>(R.id.editTextName)
        val textViewPath = dialogView.findViewById<TextView>(R.id.textViewPath)
        val buttonSelectPath = dialogView.findViewById<Button>(R.id.buttonSelectPath)
        val editTextDescription = dialogView.findViewById<TextInputEditText>(R.id.editTextDescription)
        val spinnerColor = dialogView.findViewById<Spinner>(R.id.spinnerColor)
        val textViewTitle = dialogView.findViewById<TextView>(R.id.textViewTitle)

        textViewTitle.text = "Редактировать закладку"
        
        // Заполнение существующими данными
        editTextName.setText(bookmark.name)
        textViewPath.text = bookmark.path
        editTextDescription.setText(bookmark.description)
        selectedPath = bookmark.path
        selectedIsDirectory = bookmark.isDirectory

        // Настройка спиннера выбора цвета
        val colors = BookmarkColor.values().map { it.displayName }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColor.adapter = arrayAdapter
        
        spinnerColor.setSelection(bookmark.color.ordinal)

        // Отключение выбора пути при редактировании
        buttonSelectPath.visibility = Button.GONE

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.buttonSave).setOnClickListener {
            val name = editTextName.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val colorPosition = spinnerColor.selectedItemPosition

            if (name.isEmpty()) {
                Toast.makeText(this, "Введите название закладки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedBookmark = FileBookmark(
                id = bookmark.id,
                name = name,
                path = bookmark.path,
                description = description,
                color = BookmarkColor.values()[colorPosition],
                createdDate = bookmark.createdDate,
                isDirectory = bookmark.isDirectory
            )

            val position = adapter.getBookmarkPosition(bookmark)
            if (position >= 0) {
                adapter.updateBookmark(position, updatedBookmark)
                saveBookmarks()
                Toast.makeText(this, "Закладка обновлена", Toast.LENGTH_SHORT).show()
            }
            
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(bookmark: FileBookmark) {
        AlertDialog.Builder(this)
            .setTitle("Удалить закладку?")
            .setMessage("Вы уверены, что хотите удалить закладку \"${bookmark.name}\"?")
            .setPositiveButton("Удалить") { _, _ ->
                val position = adapter.getBookmarkPosition(bookmark)
                if (position >= 0) {
                    adapter.removeBookmark(position)
                    saveBookmarks()
                    Toast.makeText(this, "Закладка удалена", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }



    private fun saveBookmarks() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = prefs.edit()
        
        val jsonArray = JSONArray()
        for (bookmark in bookmarks) {
            val jsonObject = JSONObject()
            jsonObject.put("id", bookmark.id)
            jsonObject.put("name", bookmark.name)
            jsonObject.put("path", bookmark.path)
            jsonObject.put("description", bookmark.description)
            jsonObject.put("color", bookmark.color.name)
            jsonObject.put("createdDate", bookmark.createdDate)
            jsonObject.put("isDirectory", bookmark.isDirectory)
            jsonArray.put(jsonObject)
        }
        
        editor.putString(KEY_BOOKMARKS, jsonArray.toString())
        editor.apply()
    }

    private fun loadBookmarks() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val jsonString = prefs.getString(KEY_BOOKMARKS, null) ?: return
        
        try {
            val jsonArray = JSONArray(jsonString)
            bookmarks.clear()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val bookmark = FileBookmark(
                    id = jsonObject.getLong("id"),
                    name = jsonObject.getString("name"),
                    path = jsonObject.getString("path"),
                    description = jsonObject.getString("description"),
                    color = BookmarkColor.valueOf(jsonObject.getString("color")),
                    createdDate = jsonObject.getLong("createdDate"),
                    isDirectory = jsonObject.getBoolean("isDirectory")
                )
                bookmarks.add(bookmark)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
