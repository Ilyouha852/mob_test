package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IBookmarkRepository
import com.example.mobdev_lab3.model.FileBookmark

class SaveBookmarksUseCase(private val repository: IBookmarkRepository) {
    operator fun invoke(bookmarks: List<FileBookmark>) = repository.saveBookmarks(bookmarks)
}
