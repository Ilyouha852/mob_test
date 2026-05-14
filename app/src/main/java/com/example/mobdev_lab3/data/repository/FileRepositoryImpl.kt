package com.example.mobdev_lab3.data.repository

import com.example.mobdev_lab3.data.storage.LocalFileRepository
import com.example.mobdev_lab3.domain.repository.IFileRepository
import com.example.mobdev_lab3.domain.model.FileItem
import com.example.mobdev_lab3.domain.model.SortMode

import javax.inject.Inject

class FileRepositoryImpl @Inject constructor() : IFileRepository {

 private val localRepository = LocalFileRepository()

    override suspend fun getFilesSorted(path: String, sortMode: SortMode): List<FileItem> =
        localRepository.getFilesSorted(path, sortMode)
}
