package com.example.mobdev_lab3.presentation.filedetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobdev_lab3.data.repository.BookmarkRepositoryImpl
import com.example.mobdev_lab3.data.repository.FileMetadataRepositoryImpl
import com.example.mobdev_lab3.data.repository.FileOperationsRepositoryImpl
import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.usecase.DeleteFileUseCase
import com.example.mobdev_lab3.domain.usecase.GetFileContentUseCase
import com.example.mobdev_lab3.domain.usecase.GetFileMetadataByPathUseCase
import com.example.mobdev_lab3.domain.usecase.RenameFileUseCase
import com.example.mobdev_lab3.domain.usecase.SaveBookmarksUseCase
import com.example.mobdev_lab3.domain.usecase.ToggleFileInDatabaseUseCase
import com.example.mobdev_lab3.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FileDetailUiState(
    val file: FileItem? = null,
    val content: String = "",
    val isInDatabase: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class FileDetailEvent {
    object Deleted : FileDetailEvent()
    data class Renamed(val newPath: String, val newItem: FileItem) : FileDetailEvent()
    object BookmarkAdded : FileDetailEvent()
    data class Error(val message: String) : FileDetailEvent()
}

class FileDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val fileOpsRepo = FileOperationsRepositoryImpl()
    private val metadataRepo = FileMetadataRepositoryImpl()
    private val bookmarkRepo = BookmarkRepositoryImpl(application)

    private val getContentUseCase = GetFileContentUseCase(fileOpsRepo)
    private val deleteFileUseCase = DeleteFileUseCase(fileOpsRepo)
    private val renameFileUseCase = RenameFileUseCase(fileOpsRepo)
    private val getMetadataByPathUseCase = GetFileMetadataByPathUseCase(metadataRepo)
    private val toggleDatabaseUseCase = ToggleFileInDatabaseUseCase(metadataRepo)
    private val saveBookmarksUseCase = SaveBookmarksUseCase(bookmarkRepo)

    private val _uiState = MutableLiveData(FileDetailUiState())
    val uiState: LiveData<FileDetailUiState> = _uiState

    private val _event = MutableLiveData<FileDetailEvent?>()
    val event: LiveData<FileDetailEvent?> = _event

    private var currentMetadata: FileMetadata? = null

    fun loadFile(filePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true, error = null)
            try {
                val fileItem = withContext(Dispatchers.IO) {
                    FileItem.fromFile(java.io.File(filePath))
                }
                val content = withContext(Dispatchers.IO) {
                    getContentUseCase(filePath, fileItem.isDirectory)
                }
                currentMetadata = withContext(Dispatchers.IO) {
                    getMetadataByPathUseCase(filePath)
                }
                _uiState.value = FileDetailUiState(
                    file = fileItem,
                    content = content,
                    isInDatabase = currentMetadata != null,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value?.copy(
                    isLoading = false,
                    error = "Ошибка загрузки файла: ${e.message}"
                )
            }
        }
    }

    fun deleteFile() {
        val file = _uiState.value?.file ?: return
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) { deleteFileUseCase(file.path) }
            if (success) _event.value = FileDetailEvent.Deleted
            else _event.value = FileDetailEvent.Error("Не удалось удалить файл или папку")
        }
    }

    fun renameFile(newName: String) {
        val file = _uiState.value?.file ?: return
        if (newName.isBlank() || newName == file.name) return
        viewModelScope.launch {
            val newPath = withContext(Dispatchers.IO) { renameFileUseCase(file.path, newName) }
            if (newPath != null) {
                val newItem = withContext(Dispatchers.IO) { FileItem.fromFile(java.io.File(newPath)) }
                _uiState.value = _uiState.value?.copy(file = newItem)
                _event.value = FileDetailEvent.Renamed(newPath, newItem)
            } else {
                _event.value = FileDetailEvent.Error("Не удалось переименовать файл")
            }
        }
    }

    fun toggleDatabaseStatus() {
        val file = _uiState.value?.file ?: return
        viewModelScope.launch {
            val isNowInDb = withContext(Dispatchers.IO) {
                toggleDatabaseUseCase(file, currentMetadata)
            }
            currentMetadata = if (isNowInDb) withContext(Dispatchers.IO) {
                getMetadataByPathUseCase(file.path)
            } else null
            _uiState.value = _uiState.value?.copy(isInDatabase = isNowInDb)
        }
    }

    fun consumeEvent() {
        _event.value = null
    }
}
