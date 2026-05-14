package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IBookmarkRepository
import com.example.mobdev_lab3.domain.model.FileBookmark

import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(private val repository: IBookmarkRepository) {
    operator fun invoke(): List<FileBookmark> = repository.loadBookmarks()
}