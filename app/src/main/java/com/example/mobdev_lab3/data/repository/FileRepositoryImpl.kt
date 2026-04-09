package com.example.mobdev_lab3.data.repository

import com.example.mobdev_lab3.domain.repository.IFileRepository
import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.model.SortMode
import com.example.mobdev_lab3.repository.LocalFileRepository

class FileRepositoryImpl : IFileRepository {

    private val localRepository = LocalFileRepository()

    override suspend fun getFilesSorted(path: String, sortMode: SortMode): List<FileItem> =
        localRepository.getFilesSorted(path, sortMode)
}
