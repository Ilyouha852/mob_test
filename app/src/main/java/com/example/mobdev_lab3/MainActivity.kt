package com.example.mobdev_lab3

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.model.SortMode
import com.example.mobdev_lab3.viewmodel.FileManagerViewModel
import androidx.core.net.toUri

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var radioByName: RadioButton
    private lateinit var radioByType: RadioButton
    private lateinit var radioByDate: RadioButton
    private lateinit var textViewFiles: TextView
    private lateinit var listViewFiles: ListView
    private lateinit var buttonUp: Button
    private lateinit var buttonHome: Button
    private lateinit var buttonFileManagement: Button

    private lateinit var toolbar: Toolbar
    private val viewModel: FileManagerViewModel by viewModels()

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        const val EXTRA_CURRENT_PATH = "current_path"
        const val REQUEST_CODE_SETTINGS = 1002
        const val REQUEST_CODE_FILE_MANAGEMENT = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация базы данных
        com.example.mobdev_lab3.database.DatabaseManager.init(this)

        // Инициализация UI элементов
        radioByName = findViewById(R.id.radioByName)
        radioByType = findViewById(R.id.radioByType)
        radioByDate = findViewById(R.id.radioByDate)
        textViewFiles = findViewById(R.id.textViewFiles)
        listViewFiles = findViewById(R.id.listViewFiles)
        buttonUp = findViewById(R.id.buttonUp)
        buttonHome = findViewById(R.id.buttonHome)
        buttonFileManagement = findViewById(R.id.buttonFileManagement)

        toolbar = findViewById(R.id.toolbar) // Находим Toolbar по ID
        setSupportActionBar(toolbar) // Устанавливаем Toolbar как ActionBar

        setupUI()
        setupObservers()

        // Применяем сохранённые настройки
        applySavedSettings()

        // Проверяем разрешения и загружаем файлы
        if (checkPermissions()) {
            val currentPath = intent.getStringExtra(EXTRA_CURRENT_PATH)
            if (currentPath != null) {
                viewModel.setCurrentPath(currentPath)
            } else {
                viewModel.loadFiles()
            }
        } else {
            requestPermissions()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        
        val currentPath = intent.getStringExtra(EXTRA_CURRENT_PATH)
        if (currentPath != null) {
            viewModel.setCurrentPath(currentPath)
        }
    }

    private fun setupUI() {
        // Настройка слушателей RadioButton
        radioByName.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setSortMode(SortMode.BY_NAME)
            }
        }

        radioByType.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setSortMode(SortMode.BY_TYPE)
            }
        }

        radioByDate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setSortMode(SortMode.BY_DATE)
            }
        }

        // Настройка кнопок навигации
        buttonUp.setOnClickListener {
            viewModel.goUpDirectory()
        }

        buttonHome.setOnClickListener {
            viewModel.goToHomeDirectory()
        }

        buttonFileManagement.setOnClickListener {
            val intent = Intent(this, FileManagementActivity::class.java)
            intent.putExtra(FileManagementActivity.EXTRA_CURRENT_PATH, viewModel.getCurrentPath())
            startActivityForResult(intent, REQUEST_CODE_FILE_MANAGEMENT)
        }

        // Настройка ListView
        listViewFiles.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val files = viewModel.files.value ?: return@OnItemClickListener
            if (position < files.size) {
                val selectedFile = files[position]
                if (selectedFile.isDirectory) {
                    viewModel.navigateToDirectory(selectedFile.path)
                } else {
                    openFileDetail(selectedFile)
                }
            }
        }

        listViewFiles.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
            val files = viewModel.files.value ?: return@OnItemLongClickListener false
            if (position < files.size) {
                val selectedFile = files[position]
                openFileDetail(selectedFile)
                true
            } else {
                false
            }
        }

        // Устанавливаем начальное состояние
        radioByName.isChecked = true
    }

    private fun setupObservers() {
        // Наблюдаем за изменениями списка файлов
        viewModel.files.observe(this, Observer { files ->
            updateFileList()
        })

        // Наблюдаем за изменениями режима сортировки
        viewModel.sortMode.observe(this, Observer { sortMode ->
            updateFileList()
        })

        // Наблюдаем за состоянием загрузки
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                textViewFiles.text = "Загрузка файлов..."
            }
        })

        // Наблюдаем за ошибками
        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            if (errorMessage != null) {
                textViewFiles.text = errorMessage
            }
        })
    }

    private fun updateFileList() {
        val files = viewModel.files.value ?: return
        val currentPath = viewModel.getCurrentPath()

        // Обновляем TextView с информацией
        val header = "Путь: $currentPath\nНайдено файлов: ${files.size}\n\n"
        textViewFiles.text = header

        // Обновляем ListView
        val fileNames = files.map { file ->
            val icon = if (file.isDirectory) "📁" else "📄"
            val size = file.getFormattedSize()
            val date = file.getFormattedDate()
            "$icon ${file.name}\n   $size | $date"
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
        listViewFiles.adapter = adapter
    }

    private fun openFileDetail(file: FileItem) {
        val intent = Intent(this, FileDetailActivity::class.java)
        intent.putExtra(FileDetailActivity.EXTRA_FILE_PATH, file.path)
        startActivityForResult(intent, PERMISSION_REQUEST_CODE)
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = "package:$packageName".toUri()
                startActivityForResult(intent, PERMISSION_REQUEST_CODE)
            } catch (_: Exception) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.loadFiles()
            } else {
                Toast.makeText(this, "Разрешение необходимо для работы с файлами", Toast.LENGTH_LONG).show()
                textViewFiles.text = "Необходимо разрешение для доступа к файлам"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (checkPermissions()) {
                    viewModel.loadFiles()
                } else {
                    Toast.makeText(this, "Разрешение необходимо для работы с файлами", Toast.LENGTH_LONG).show()
                    textViewFiles.text = "Необходимо разрешение для доступа к файлам"
                }
            }

            REQUEST_CODE_SETTINGS -> {
                if (resultCode == RESULT_OK) {
                    data?.let { intent ->
                        val sortMode = intent.getStringExtra(SettingsActivity.EXTRA_SORT_MODE)
                        val showHidden = intent.getBooleanExtra(SettingsActivity.EXTRA_SHOW_HIDDEN, false)
                        val showSystemFiles = intent.getBooleanExtra(SettingsActivity.EXTRA_SHOW_SYSTEM_FILES, false)

                        // Применяем настройки
                        when (sortMode) {
                            SortMode.BY_NAME.name -> {
                                radioByName.isChecked = true
                                viewModel.setSortMode(SortMode.BY_NAME)
                            }
                            SortMode.BY_TYPE.name -> {
                                radioByType.isChecked = true
                                viewModel.setSortMode(SortMode.BY_TYPE)
                            }
                            SortMode.BY_DATE.name -> {
                                radioByDate.isChecked = true
                                viewModel.setSortMode(SortMode.BY_DATE)
                            }
                        }

                        viewModel.setShowHidden(showHidden)
                        viewModel.setShowSystemFiles(showSystemFiles)
                    }
                }
            }

            REQUEST_CODE_FILE_MANAGEMENT -> {
                if (resultCode == RESULT_OK) {
                    viewModel.loadFiles()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_home -> {
                viewModel.goToHomeDirectory()
                true
            }
            R.id.menu_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_SETTINGS)
                true
            }
            R.id.menu_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_bookmarks -> {
                val intent = Intent(this, BookmarksActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_file_management -> {
                val intent = Intent(this, FileManagementActivity::class.java)
                intent.putExtra(FileManagementActivity.EXTRA_CURRENT_PATH, viewModel.getCurrentPath())
                startActivity(intent)
                true
            }
            R.id.menu_storage_lab -> {
                val intent = Intent(this, StorageLabActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_concurrency_lab -> {
                val intent = Intent(this, ConcurrencyLabActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_file_metadata -> {
                val intent = Intent(this, FileMetadataActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun applySavedSettings() {
        val sharedPreferences = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE)
        val showHidden = sharedPreferences.getBoolean(SettingsActivity.KEY_SHOW_HIDDEN, false)
        val showSystemFiles = sharedPreferences.getBoolean(SettingsActivity.KEY_SHOW_SYSTEM_FILES, false)

        viewModel.setShowHidden(showHidden)
        viewModel.setShowSystemFiles(showSystemFiles)
    }

}
