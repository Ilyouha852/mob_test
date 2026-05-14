package com.example.mobdev_lab3.domain.repository

import com.example.mobdev_lab3.domain.model.FileBookmark

interface IBookmarkRepository {
    fun loadBookmarks(): List<FileBookmark>
    fun saveBookmarks(bookmarks: List<FileBookmark>)
}
