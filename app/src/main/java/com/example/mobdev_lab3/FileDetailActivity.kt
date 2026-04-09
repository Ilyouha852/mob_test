package com.example.mobdev_lab3

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.database.repository.FileMetadataRepository
import com.example.mobdev_lab3.database.entity.FileMetadata
import java.io.File
import java.util.Date

@Suppress("DEPRECATION")
class FileDetailActivity : AppCompatActivity() {
    
    private lateinit var textViewFileName: TextView
    private lateinit var textViewFilePath: TextView
    private lateinit var textViewFileSize: TextView
    private lateinit var textViewFileType: TextView
    private lateinit var textViewFileDate: TextView
    private lateinit var textViewFileContent: TextView
    private lateinit var scrollViewContent: ScrollView
    private lateinit var buttonBack: Button
    private lateinit var buttonOpen: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonRename: Button
    private lateinit var buttonAddToBookmarks: Button
    private lateinit var buttonAddToDatabase: Button
    
    private var fileItem: FileItem? = null
    private val fileRepository = FileMetadataRepository()
    private var isFileInDatabase = false
    
    companion object {
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_FILE_ITEM = "file_item"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_detail)
        
        initViews()
        setupClickListeners()
        loadFileData()
    }
    
    private fun initViews() {
        textViewFileName = findViewById(R.id.textViewFileName)
        textViewFilePath = findViewById(R.id.textViewFilePath)
        textViewFileSize = findViewById(R.id.textViewFileSize)
        textViewFileType = findViewById(R.id.textViewFileType)
        textViewFileDate = findViewById(R.id.textViewFileDate)
        textViewFileContent = findViewById(R.id.textViewFileContent)
        scrollViewContent = findViewById(R.id.scrollViewContent)
        buttonBack = findViewById(R.id.buttonBack)
        buttonOpen = findViewById(R.id.buttonOpen)
        buttonDelete = findViewById(R.id.buttonDelete)
        buttonRename = findViewById(R.id.buttonRename)
        buttonAddToBookmarks = findViewById(R.id.buttonAddToBookmarks)
        buttonAddToDatabase = findViewById(R.id.buttonAddToDatabase)
    }
    
    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            finish()
        }
        
        buttonOpen.setOnClickListener {
            openFile()
        }
        
        buttonDelete.setOnClickListener {
            deleteFile()
        }
        
        buttonRename.setOnClickListener {
            renameFile()
        }

        buttonAddToBookmarks.setOnClickListener {
            addToBookmarks()
        }

        buttonAddToDatabase.setOnClickListener {
            toggleDatabaseStatus()
        }
    }
    
    private fun loadFileData() {
        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        val fileItemData = intent.getSerializableExtra(EXTRA_FILE_ITEM) as? FileItem
        
        if (filePath != null) {
            val file = File(filePath)
            fileItem = FileItem.fromFile(file)
        } else if (fileItemData != null) {
            fileItem = fileItemData
        }
        
        fileItem?.let { file ->
            checkDatabaseStatus(file)
            displayFileInfo(file)
            loadFileContent(file)
        } ?: run {
            Toast.makeText(this, "Ошибка загрузки информации о файле", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun displayFileInfo(file: FileItem) {
        textViewFileName.text = "Имя: ${file.name}"
        textViewFilePath.text = "Путь: ${file.path}"
        textViewFileSize.text = "Размер: ${file.getFormattedSize()}"
        textViewFileType.text = "Тип: ${file.getFileType()}"
        textViewFileDate.text = "Дата изменения: ${file.getFormattedDate()}"
    }
    
    @SuppressLint("SetTextI18n")
    private fun loadFileContent(file: FileItem) {
        if (file.isDirectory) {
            textViewFileContent.text = "Это папка. Содержимое:\n\n" + getDirectoryContents(file.path)
            buttonOpen.text = "Открыть папку"
        } else {
            val content = getFileContent(file.path)
            textViewFileContent.text = "Содержимое файла:\n\n$content"
            buttonOpen.text = "Открыть файл"
        }
    }
    
    private fun getDirectoryContents(path: String): String {
        return try {
            val directory = File(path)
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                files?.joinToString("\n") { file ->
                    val icon = if (file.isDirectory) "📁" else "📄"
                    "$icon ${file.name}"
                }
                    ?: "Папка пуста"
            } else {
                "Папка не найдена"
            }
        } catch (e: Exception) {
            "Ошибка чтения папки: ${e.message}"
        }
    }
    
    private fun getFileContent(path: String): String {
        return try {
            val file = File(path)
            if (file.exists() && file.isFile) {
                when (file.extension.lowercase()) {
                    "txt", "log", "md" -> {
                        file.readText(Charsets.UTF_8).take(1000) + 
                        if (file.length() > 1000) "\n\n... (показаны первые 1000 символов)" else ""
                    }
                    "jpg", "jpeg", "png", "gif", "bmp" -> "Изображение (предпросмотр недоступен)"
                    "mp4", "avi", "mkv", "mov" -> "Видео файл (предпросмотр недоступен)"
                    "mp3", "wav", "flac" -> "Аудио файл (предпросмотр недоступен)"
                    else -> "Бинарный файл (предпросмотр недоступен)"
                }
            } else {
                "Файл не найден"
            }
        } catch (e: Exception) {
            "Ошибка чтения файла: ${e.message}"
        }
    }
    
    private fun openFile() {
        fileItem?.let { file ->
            if (file.isDirectory) {
                // Переходим к главной активности с новой папкой
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_CURRENT_PATH, file.path)
                startActivity(intent)
            } else {
                // Пытаемся открыть файл системным приложением
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(android.net.Uri.fromFile(File(file.path)), getMimeType(file.extension))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Не удалось открыть файл: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteFile() {
        fileItem?.let { file ->
            try {
                val fileObj = File(file.path)
                if (fileObj.exists()) {
                    if (deleteRecursively(fileObj)) {
                        Toast.makeText(this, "Файл или папка удалены", Toast.LENGTH_SHORT).show()

                        val resultIntent = Intent()
                        resultIntent.putExtra("deleted_path", file.path)
                        setResult(RESULT_OK, resultIntent)

                        finish()
                    } else {
                        Toast.makeText(this, "Не удалось удалить файл или папку", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка удаления: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteRecursively(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                val files = file.listFiles()
                if (files != null) {
                    for (child in files) {
                        deleteRecursively(child)
                    }
                }
            }
            file.delete()
        } catch (_: Exception) {
            false
        }
    }


    private fun renameFile() {
        fileItem?.let { file ->
            val input = EditText(this)
            input.setText(file.name)
            
            AlertDialog.Builder(this)
                .setTitle("Переименовать файл")
                .setView(input)
                .setPositiveButton("Переименовать") { _, _ ->
                    val newName = input.text.toString().trim()
                    if (newName.isNotEmpty() && newName != file.name) {
                        try {
                            val oldFile = File(file.path)
                            val newFile = File(oldFile.parent, newName)
                            if (oldFile.renameTo(newFile)) {
                                Toast.makeText(this, "Файл переименован", Toast.LENGTH_SHORT).show()
                                loadFileData() // Перезагружаем данные
                            } else {
                                Toast.makeText(this, "Не удалось переименовать файл", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, "Ошибка переименования: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }
    
    private fun addToBookmarks() {
        fileItem?.let { file ->
            val intent = Intent(this, BookmarksActivity::class.java)
            intent.putExtra(BookmarksActivity.EXTRA_ADD_BOOKMARK_PATH, file.path)
            startActivity(intent)
        }
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "txt" -> "text/plain"
            "html", "htm" -> "text/html"
            "css" -> "text/css"
            "js" -> "text/javascript"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            else -> "*/*"
        }
    }
    private fun checkDatabaseStatus(file: FileItem) {
        val metadata = fileRepository.getMetadataByPath(file.path)
        isFileInDatabase = metadata != null
        updateDatabaseButton()
    }

    private fun updateDatabaseButton() {
        if (isFileInDatabase) {
            buttonAddToDatabase.text = "Remove from Database"
        } else {
            buttonAddToDatabase.text = "Add to Database"
        }
    }

    private fun toggleDatabaseStatus() {
        fileItem?.let { file ->
            if (isFileInDatabase) {
                // Remove
                val metadata = fileRepository.getMetadataByPath(file.path)
                metadata?.let {
                    fileRepository.deleteFileMetadata(it.id)
                    isFileInDatabase = false
                    Toast.makeText(this, "Removed from Database", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Add
                val metadata = FileMetadata()
                metadata.filePath = file.path
                metadata.fileName = file.name
                metadata.lastAccessDate = Date().time
                metadata.isFavorite = false
                fileRepository.createFileMetadata(metadata)
                isFileInDatabase = true
                Toast.makeText(this, "Added to Database", Toast.LENGTH_SHORT).show()
            }
            updateDatabaseButton()
        }
    }
}
