package com.example.mobdev_lab3.data.storage

import com.example.mobdev_lab3.domain.model.FileItem
import com.example.mobdev_lab3.domain.model.SortMode

abstract class FileRepository {
    abstract suspend fun getFiles(path: String): List<FileItem>
    abstract suspend fun getFilesSorted(path: String, sortMode: SortMode): List<FileItem>
}
