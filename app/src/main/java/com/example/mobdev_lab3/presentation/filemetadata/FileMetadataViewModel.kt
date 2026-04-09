package com.example.mobdev_lab3.presentation.filemetadata

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobdev_lab3.data.repository.BookmarkRepositoryImpl
import com.example.mobdev_lab3.data.repository.FileMetadataRepositoryImpl
import com.example.mobdev_lab3.data.repository.TagRepositoryImpl
import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.domain.usecase.AssignTagsToFileUseCase
import com.example.mobdev_lab3.domain.usecase.CreateTagUseCase
import com.example.mobdev_lab3.domain.usecase.DeleteFileMetadataUseCase
import com.example.mobdev_lab3.domain.usecase.GetAllFileMetadataUseCase
import com.example.mobdev_lab3.domain.usecase.GetAllTagsUseCase
import com.example.mobdev_lab3.domain.usecase.GetTagsByFileUseCase
import com.example.mobdev_lab3.domain.usecase.UpdateFileMetadataUseCase
import com.example.mobdev_lab3.manager.BookmarksManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

data class FileMetadataUiState(
    val files: List<FileMetadata> = emptyList(),
    val tagsMap: Map<Long, List<String>> = emptyMap(),
    val availableTags: List<Tag> = emptyList(),
    val isLoading: Boolean = false
)

sealed class FileMetadataEvent {
    data class Message(val text: String) : FileMetadataEvent()
}

class FileMetadataViewModel(application: Application) : AndroidViewModel(application) {

    private val metadataRepo = FileMetadataRepositoryImpl()
    private val tagRepo = TagRepositoryImpl()
    private val bookmarkRepo = BookmarkRepositoryImpl(application)

    private val getAllMetadataUseCase = GetAllFileMetadataUseCase(metadataRepo)
    private val updateMetadataUseCase = UpdateFileMetadataUseCase(metadataRepo)
    private val deleteMetadataUseCase = DeleteFileMetadataUseCase(metadataRepo)
    private val getAllTagsUseCase = GetAllTagsUseCase(tagRepo)
    private val getTagsByFileUseCase = GetTagsByFileUseCase(tagRepo)
    private val createTagUseCase = CreateTagUseCase(tagRepo)
    private val assignTagsUseCase = AssignTagsToFileUseCase(metadataRepo, tagRepo)

    private val _uiState = MutableLiveData(FileMetadataUiState())
    val uiState: LiveData<FileMetadataUiState> = _uiState

    private val _event = MutableLiveData<FileMetadataEvent?>()
    val event: LiveData<FileMetadataEvent?> = _event

    var searchQuery: String = ""
        private set
    var tagFilter: Tag? = null
        private set
    var favoriteFilter: Boolean = false
        private set

    init {
        loadData()
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        loadData()
    }

    fun setTagFilter(tag: Tag?) {
        tagFilter = tag
        loadData()
    }

    fun setFavoriteFilter(enabled: Boolean) {
        favoriteFilter = enabled
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value?.copy(isLoading = true)
            val context = getApplication<Application>()
            val result = withContext(Dispatchers.IO) {
                var files = getAllMetadataUseCase()

                if (searchQuery.isNotEmpty())
                    files = files.filter { it.fileName.contains(searchQuery, ignoreCase = true) }
                if (favoriteFilter)
                    files = files.filter { it.isFavorite == true }
                tagFilter?.let { tag ->
                    val ids = metadataRepo.getFilesByTag(tag.id).map { it.id }.toSet()
                    files = files.filter { it.id in ids }
                }

                val bookmarkedPaths = BookmarksManager.getBookmarks(context).map { it.path }.toSet()
                files.forEach { file ->
                    if (bookmarkedPaths.contains(file.filePath) && file.isFavorite != true) {
                        file.isFavorite = true
                        updateMetadataUseCase(file)
                    }
                }

                val tagsMap = files.associate { file ->
                    file.id to getTagsByFileUseCase(file.id).map { it.name }
                }
                val allTags = getAllTagsUseCase()
                Triple(files, tagsMap, allTags)
            }
            _uiState.value = FileMetadataUiState(
                files = result.first,
                tagsMap = result.second,
                availableTags = result.third,
                isLoading = false
            )
        }
    }

    fun toggleFavorite(file: FileMetadata, onNeedBookmarkDialog: (FileMetadata, List<Tag>) -> Unit) {
        val newStatus = !(file.isFavorite ?: false)
        if (newStatus) {
            val fileTags = getTagsByFileUseCase(file.id)
            onNeedBookmarkDialog(file, fileTags)
        } else {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    file.isFavorite = false
                    updateMetadataUseCase(file)
                    val context = getApplication<Application>()
                    if (BookmarksManager.isBookmarked(context, file.filePath)) {
                        BookmarksManager.removeBookmarkByPath(context, file.filePath)
                    }
                }
                _event.value = FileMetadataEvent.Message("Удалено из закладок")
                loadData()
            }
        }
    }

    fun confirmAddToFavorites(file: FileMetadata) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                file.isFavorite = true
                updateMetadataUseCase(file)
            }
            loadData()
        }
    }

    fun assignTags(fileId: Long, selectedTags: List<Tag>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { assignTagsUseCase(fileId, selectedTags) }
            _event.value = FileMetadataEvent.Message("Теги обновлены")
            loadData()
        }
    }

    fun deleteMetadata(file: FileMetadata) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { deleteMetadataUseCase(file.id) }
            _event.value = FileMetadataEvent.Message("Файл удалён из базы данных")
            loadData()
        }
    }

    fun createTag(name: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val tag = Tag().apply {
                    this.name = name
                    createdDate = Date().time
                }
                createTagUseCase(tag)
            }
            _event.value = FileMetadataEvent.Message("Тег создан!")
            loadData()
        }
    }

    fun consumeEvent() {
        _event.value = null
    }
}
