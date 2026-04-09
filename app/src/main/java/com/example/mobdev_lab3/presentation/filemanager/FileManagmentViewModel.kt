package com.example.mobdev_lab3.presentation.filemanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobdev_lab3.data.repository.FileOperationsRepositoryImpl
import com.example.mobdev_lab3.domain.usecase.CreateDirectoryUseCase
import com.example.mobdev_lab3.domain.usecase.CreateFileUseCase
import com.example.mobdev_lab3.domain.usecase.DeleteFileUseCase
import com.example.mobdev_lab3.domain.usecase.ListFilesInDirectoryUseCase
import com.example.mobdev_lab3.domain.usecase.RenameFileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class FileManagementUiState(
    val currentPath: String = "",
    val files: List<File> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class FileManagementEvent {
    object OperationSuccess : FileManagementEvent()
    data class Error(val message: String) : FileManagementEvent()
}

class FileManagementViewModel : ViewModel() {

    private val repository = FileOperationsRepositoryImpl()

    private val listFilesUseCase = ListFilesInDirectoryUseCase(repository)
    private val createFileUseCase = CreateFileUseCase(repository)
    private val createDirUseCase = CreateDirectoryUseCase(repository)
    private val deleteFileUseCase = DeleteFileUseCase(repository)
    private val renameFileUseCase = RenameFileUseCase(repository)

    private val _uiState = MutableLiveData(FileManagementUiState())
    val uiState: LiveData<FileManagementUiState> = _uiState

    private val _event = MutableLiveData<FileManagementEvent?>()
    val event: LiveData<FileManagementEvent?> = _event

    fun initialize(startPath: String) {
        if (_uiState.value?.currentPath.isNullOrEmpty()) {
            val path = startPath.ifEmpty { repository.getDefaultStoragePath() }
            navigateTo(path)
        }
    }

    fun navigateTo(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
            val files = withContext(Dispatchers.IO) { listFilesUseCase(path) }
            _uiState.value = FileManagementUiState(
                currentPath = path,
                files = files,
                isLoading = false
            )
        }
    }

    fun goUp() {
        val current = _uiState.value?.currentPath ?: return
        val parent = File(current).parentFile
        if (parent != null && parent.exists()) navigateTo(parent.absolutePath)
    }

    fun goHome() = navigateTo(repository.getDefaultStoragePath())

    fun createFile(name: String) {
        val path = _uiState.value?.currentPath ?: return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { createFileUseCase(path, name) }
            if (ok) {
                _event.value = FileManagementEvent.OperationSuccess
                navigateTo(path)
            } else {
                _event.value = FileManagementEvent.Error("Не удалось создать файл")
            }
        }
    }

    fun createDirectory(name: String) {
        val path = _uiState.value?.currentPath ?: return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { createDirUseCase(path, name) }
            if (ok) {
                _event.value = FileManagementEvent.OperationSuccess
                navigateTo(path)
            } else {
                _event.value = FileManagementEvent.Error("Не удалось создать папку")
            }
        }
    }

    fun deleteFile(filePath: String) {
        val path = _uiState.value?.currentPath ?: return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { deleteFileUseCase(filePath) }
            if (ok) {
                _event.value = FileManagementEvent.OperationSuccess
                navigateTo(path)
            } else {
                _event.value = FileManagementEvent.Error("Не удалось удалить файл")
            }
        }
    }

    fun renameFile(oldPath: String, newName: String) {
        val path = _uiState.value?.currentPath ?: return
        viewModelScope.launch {
            val newPath = withContext(Dispatchers.IO) { renameFileUseCase(oldPath, newName) }
            if (newPath != null) {
                _event.value = FileManagementEvent.OperationSuccess
                navigateTo(path)
            } else {
                _event.value = FileManagementEvent.Error("Не удалось переименовать файл")
            }
        }
    }

    fun consumeEvent() {
        _event.value = null
    }
}
