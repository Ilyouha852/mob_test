package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IBookmarkRepository
import com.example.mobdev_lab3.model.FileBookmark

class GetBookmarksUseCase(private val repository: IBookmarkRepository) {
    operator fun invoke(): List<FileBookmark> = repository.loadBookmarks()
}
