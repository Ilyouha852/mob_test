package com.example.mobdev_lab3

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FileManagementActivity : AppCompatActivity() {
    
    private lateinit var textViewCurrentPath: TextView
    private lateinit var buttonCreateFile: Button
    private lateinit var buttonCreateFolder: Button
    private lateinit var buttonBack: Button
    private lateinit var listViewFiles: ListView
    private lateinit var buttonUp: Button
    private lateinit var buttonHome: Button
    
    private var currentPath: String = ""
    private var fileList = mutableListOf<File>()
    
    companion object {
        const val EXTRA_CURRENT_PATH = "current_path"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_management)
        
        initViews()
        setupClickListeners()
        loadCurrentPath()
        loadFiles()
    }
    
    private fun initViews() {
        textViewCurrentPath = findViewById(R.id.textViewCurrentPath)
        buttonCreateFile = findViewById(R.id.buttonCreateFile)
        buttonCreateFolder = findViewById(R.id.buttonCreateFolder)
        buttonBack = findViewById(R.id.buttonBack)
        listViewFiles = findViewById(R.id.listViewFiles)
        buttonUp = findViewById(R.id.buttonUp)
        buttonHome = findViewById(R.id.buttonHome)
    }
    
    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            finish()
        }
        
        buttonCreateFile.setOnClickListener {
            createNewFile()
        }
        
        buttonCreateFolder.setOnClickListener {
            createNewFolder()
        }
        
        buttonUp.setOnClickListener {
            goUpDirectory()
        }
        
        buttonHome.setOnClickListener {
            goToHomeDirectory()
        }
        
        listViewFiles.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedFile = fileList[position]
            if (selectedFile.isDirectory) {
                navigateToDirectory(selectedFile.absolutePath)
            } else {
                showFileOptions(selectedFile)
            }
        }
        
        listViewFiles.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val selectedFile = fileList[position]
            showFileOptions(selectedFile)
            true
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun loadCurrentPath() {
        currentPath = intent.getStringExtra(EXTRA_CURRENT_PATH) ?: getDefaultPath()
        textViewCurrentPath.text = "Текущая папка: $currentPath"
    }
    
    private fun getDefaultPath(): String {
        return try {
            val externalStorageDir = android.os.Environment.getExternalStorageDirectory()
            if (externalStorageDir != null && externalStorageDir.exists()) {
                externalStorageDir.absolutePath
            } else {
                "/storage/emulated/0"
            }
        } catch (_: Exception) {
            "/storage/emulated/0"
        }
    }
    
    private fun loadFiles() {
        try {
            val directory = File(currentPath)
            if (directory.exists() && directory.isDirectory) {
                val files = directory.listFiles()
                fileList.clear()
                if (files != null) {
                    fileList.addAll(files.sortedWith(compareBy<File> { it.isFile }.thenBy { it.name.lowercase() }))
                }
                updateFileList()
            } else {
                Toast.makeText(this, "Папка не найдена", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка загрузки файлов: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateFileList() {
        val fileNames = fileList.map { file ->
            val icon = if (file.isDirectory) "📁" else "📄"
            "$icon ${file.name}"
        }
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
        listViewFiles.adapter = adapter
    }

    private fun createNewFile() {
        val input = EditText(this)
        input.hint = "Введите имя файла"

        AlertDialog.Builder(this)
            .setTitle("Создать файл")
            .setView(input)
            .setPositiveButton("Создать") { _, _ ->
                val fileName = input.text.toString().trim()
                if (fileName.isNotEmpty()) {
                    try {
                        val newFile = File(currentPath, fileName)
                        if (newFile.createNewFile()) {
                            Toast.makeText(this, "Файл создан", Toast.LENGTH_SHORT).show()
                            loadFiles()

                            // Уведомляем MainActivity о изменении
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this, "Не удалось создать файл", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Ошибка создания файла: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    
    private fun createNewFolder() {
        val input = EditText(this)
        input.hint = "Введите имя папки"
        
        AlertDialog.Builder(this)
            .setTitle("Создать папку")
            .setView(input)
            .setPositiveButton("Создать") { _, _ ->
                val folderName = input.text.toString().trim()
                if (folderName.isNotEmpty()) {
                    try {
                        val newFolder = File(currentPath, folderName)
                        if (newFolder.mkdirs()) {
                            Toast.makeText(this, "Папка создана", Toast.LENGTH_SHORT).show()
                            loadFiles()
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this, "Не удалось создать папку", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Ошибка создания папки: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    @SuppressLint("SetTextI18n")
    private fun navigateToDirectory(path: String) {
        currentPath = path
        textViewCurrentPath.text = "Текущая папка: $currentPath"
        loadFiles()
    }
    
    private fun goUpDirectory() {
        val parentDir = File(currentPath).parentFile
        if (parentDir != null && parentDir.exists()) {
            navigateToDirectory(parentDir.absolutePath)
        } else {
            Toast.makeText(this, "Нет родительской папки", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun goToHomeDirectory() {
        navigateToDirectory(getDefaultPath())
    }
    
    private fun showFileOptions(file: File) {
        val options = arrayOf("Переименовать", "Удалить", "Подробности")
        
        AlertDialog.Builder(this)
            .setTitle("Опции для ${file.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> renameFile(file)
                    1 -> deleteFile(file)
                    2 -> showFileDetails(file)
                }
            }
            .show()
    }

    private fun renameFile(file: File) {
        val input = EditText(this)
        input.setText(file.name)

        AlertDialog.Builder(this)
            .setTitle("Переименовать")
            .setView(input)
            .setPositiveButton("Переименовать") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName != file.name) {
                    try {
                        val newFile = File(file.parent, newName)
                        if (file.renameTo(newFile)) {
                            Toast.makeText(this, "Файл переименован", Toast.LENGTH_SHORT).show()
                            loadFiles()

                            setResult(RESULT_OK)
                            finish()
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


    private fun deleteFile(file: File) {
        AlertDialog.Builder(this)
            .setTitle("Удалить файл")
            .setMessage("Вы уверены, что хотите удалить ${file.name}?")
            .setPositiveButton("Удалить") { _, _ ->
                try {
                    if (file.delete()) {
                        Toast.makeText(this, "Файл удален", Toast.LENGTH_SHORT).show()
                        loadFiles()

                        // Сообщаем MainActivity, что изменения произошли
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "Не удалось удалить файл", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка удаления: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    
    private fun showFileDetails(file: File) {
        val details = buildString {
            appendLine("Имя: ${file.name}")
            appendLine("Путь: ${file.absolutePath}")
            appendLine("Тип: ${if (file.isDirectory) "Папка" else "Файл"}")
            if (file.isFile) {
                appendLine("Размер: ${formatFileSize(file.length())}")
            }
            appendLine("Дата изменения: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified()))}")
            appendLine("Чтение: ${if (file.canRead()) "Да" else "Нет"}")
            appendLine("Запись: ${if (file.canWrite()) "Да" else "Нет"}")
        }
        
        AlertDialog.Builder(this)
            .setTitle("Подробности файла")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size Б"
            size < 1024 * 1024 -> "${size / 1024} КБ"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} МБ"
            else -> "${size / (1024 * 1024 * 1024)} ГБ"
        }
    }
}
