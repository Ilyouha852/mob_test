package com.example.mobdev_lab3.repository

import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.model.SortMode

abstract class FileRepository {
    abstract suspend fun getFiles(path: String): List<FileItem>
    abstract suspend fun getFilesSorted(path: String, sortMode: SortMode): List<FileItem>
}
