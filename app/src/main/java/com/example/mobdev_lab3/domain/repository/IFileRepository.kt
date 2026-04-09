package com.example.mobdev_lab3.domain.repository

import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.model.SortMode

interface IFileRepository {
    suspend fun getFilesSorted(path: String, sortMode: SortMode): List<FileItem>
}
