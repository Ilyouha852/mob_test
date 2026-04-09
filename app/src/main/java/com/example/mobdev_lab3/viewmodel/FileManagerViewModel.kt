package com.example.mobdev_lab3.viewmodel

import android.app.Application
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.model.SortMode
import com.example.mobdev_lab3.repository.FileRepository
import com.example.mobdev_lab3.repository.LocalFileRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: FileRepository = LocalFileRepository()
    private var loadFilesJob: Job? = null
    
    private val _files = MutableLiveData<List<FileItem>>()
    val files: LiveData<List<FileItem>> = _files
    
    private val _currentPath = MutableLiveData<String>()
    val currentPath: LiveData<String> = _currentPath
    
    private val _sortMode = MutableLiveData<SortMode>()
    val sortMode: LiveData<SortMode> = _sortMode
    
    private val _showHidden = MutableLiveData<Boolean>()
    val showHidden: LiveData<Boolean> = _showHidden
    
    private val _showSystemFiles = MutableLiveData<Boolean>()
    val showSystemFiles: LiveData<Boolean> = _showSystemFiles
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        _sortMode.value = SortMode.BY_NAME
        _showHidden.value = false
        _showSystemFiles.value = false
        loadFiles()
    }
    
    fun setSortMode(mode: SortMode) {
        if (_sortMode.value != mode) {
            _sortMode.value = mode
            loadFiles()
        }
    }

    fun loadFiles() {
        loadFilesJob?.cancel()
        loadFilesJob = viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val path = _currentPath.value ?: getDefaultPath()
                _currentPath.value = path

                // Получаем отсортированные файлы из репозитория
                val sortedFiles = repository.getFilesSorted(
                    path,
                    _sortMode.value ?: SortMode.BY_NAME
                )

                // Получаем текущие настройки
                val showHiddenFiles = _showHidden.value ?: false
                val showSystem = _showSystemFiles.value ?: false

                // Фильтруем файлы согласно настройкам
                val filteredFiles = sortedFiles.filter { file ->
                    (!file.isHidden || showHiddenFiles) && (!file.isSystemFile || showSystem)
                }

                _files.value = filteredFiles

            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки файлов: ${e.message}"
                _files.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Простейшая функция определения системного файла
    private fun isSystemFile(file: java.io.File): Boolean {
        val systemPaths = listOf("/system", "/proc", "/dev", "/data")
        return systemPaths.any { file.absolutePath.startsWith(it) }
    }

    private fun getDefaultPath(): String {
        return try {
            // Получаем корневую папку внешнего хранилища
            val externalStorageDir = Environment.getExternalStorageDirectory()
            if (externalStorageDir != null && externalStorageDir.exists()) {
                externalStorageDir.absolutePath
            } else {
                // Возврат к стандартному пути
                "/storage/emulated/0"
            }
        } catch (_: Exception) {
            // Если нет доступа к внешнему хранилищу, используем стандартный путь
            "/storage/emulated/0"
        }
    }
    
    fun getFormattedFileList(): String {
        val fileList = _files.value ?: return "Выберите режим сортировки"
        val currentMode = _sortMode.value ?: SortMode.BY_NAME
        
        if (fileList.isEmpty()) {
            return "Папка пуста"
        }
        
        val header = "Сортировка: ${getSortModeDescription(currentMode)}\n" +
                    "Путь: ${_currentPath.value}\n" +
                    "Найдено файлов: ${fileList.size}\n\n"
        
        val fileInfo = fileList.joinToString("\n") { file ->
            val icon = if (file.isDirectory) "📁" else "📄"
            val size = file.getFormattedSize()
            val date = file.getFormattedDate()
            val type = file.getFileType()
            
            "$icon ${file.name}\n" +
            "   Тип: $type | Размер: $size | Дата: $date"
        }
        
        return header + fileInfo
    }
    
    private fun getSortModeDescription(mode: SortMode): String {
        return when (mode) {
            SortMode.BY_NAME -> "По имени"
            SortMode.BY_TYPE -> "По типу"
            SortMode.BY_DATE -> "По дате"
        }
    }
    
    fun setCurrentPath(path: String) {
        _currentPath.value = path
        loadFiles()
    }
    
    fun getCurrentPath(): String {
        return _currentPath.value ?: getDefaultPath()
    }
    
    fun navigateToDirectory(path: String) {
        _currentPath.value = path
        loadFiles()
    }
    
    fun goUpDirectory() {
        val currentPath = _currentPath.value ?: return
        val parentDir = java.io.File(currentPath).parentFile
        if (parentDir != null && parentDir.exists()) {
            navigateToDirectory(parentDir.absolutePath)
        }
    }
    
    fun goToHomeDirectory() {
        navigateToDirectory(getDefaultPath())
    }
    
    fun setShowHidden(show: Boolean) {
        _showHidden.value = show
        loadFiles()
    }
    
    fun setShowSystemFiles(show: Boolean) {
        _showSystemFiles.value = show
        loadFiles()
    }
}
