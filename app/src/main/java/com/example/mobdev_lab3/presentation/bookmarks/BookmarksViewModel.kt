package com.example.mobdev_lab3.presentation.bookmarks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mobdev_lab3.data.repository.BookmarkRepositoryImpl
import com.example.mobdev_lab3.domain.usecase.GetBookmarksUseCase
import com.example.mobdev_lab3.domain.usecase.SaveBookmarksUseCase
import com.example.mobdev_lab3.model.FileBookmark

class BookmarksViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BookmarkRepositoryImpl(application)
    private val getBookmarksUseCase  = GetBookmarksUseCase(repository)
    private val saveBookmarksUseCase = SaveBookmarksUseCase(repository)

    private val _bookmarks = MutableLiveData<MutableList<FileBookmark>>(mutableListOf())
    val bookmarks: LiveData<MutableList<FileBookmark>> = _bookmarks

    init { loadBookmarks() }

    fun loadBookmarks() {
        _bookmarks.value = getBookmarksUseCase().toMutableList()
    }

    fun addBookmark(bookmark: FileBookmark) {
        val list = _bookmarks.value ?: mutableListOf()
        list.add(bookmark)
        _bookmarks.value = list
        saveBookmarksUseCase(list)
    }

    fun updateBookmark(old: FileBookmark, new: FileBookmark) {
        val list = _bookmarks.value ?: return
        val idx = list.indexOfFirst { it.id == old.id }
        if (idx >= 0) {
            list[idx] = new
            _bookmarks.value = list
            saveBookmarksUseCase(list)
        }
    }

    fun deleteBookmark(bookmark: FileBookmark) {
        val list = _bookmarks.value ?: return
        list.removeAll { it.id == bookmark.id }
        _bookmarks.value = list
        saveBookmarksUseCase(list)
    }
}
