package com.example.mobdev_lab3.repository

import com.example.mobdev_lab3.model.FileItem
import com.example.mobdev_lab3.model.SortMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalFileRepository : FileRepository() {
    
    override suspend fun getFiles(path: String): List<FileItem> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext emptyList()
            }
            
            val files = directory.listFiles()
            if (files == null) {
                return@withContext emptyList()
            }
            
            files.mapNotNull { file ->
                try {
                    FileItem.fromFile(file)
                } catch (_: Exception) {
                    // Пропускаем файлы, которые не удается прочитать
                    null
                }
            }
        } catch (_: SecurityException) {
            // Нет разрешения на доступ к папке
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getFilesSorted(path: String, sortMode: SortMode): List<FileItem> = withContext(Dispatchers.IO) {
        val files = getFiles(path)
        when (sortMode) {
            SortMode.BY_NAME -> files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() })
            SortMode.BY_TYPE -> files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenBy { it.getFileType() }.thenBy { it.name.lowercase() })
            SortMode.BY_DATE -> files.sortedWith(compareBy<FileItem> { !it.isDirectory }.thenByDescending { it.lastModified })
        }
    }
}
