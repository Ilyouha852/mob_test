package com.example.mobdev_lab3.model

import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val isHidden: Boolean,
    val isSystemFile: Boolean
) : Serializable {
    fun getFormattedSize(): String {
        return when {
            isDirectory -> "Папка"
            size < 1024 -> "$size Б"
            size < 1024 * 1024 -> "${size / 1024} КБ"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} МБ"
            else -> "${size / (1024 * 1024 * 1024)} ГБ"
        }
    }

    fun getFormattedDate(): String {
        val date = Date(lastModified)
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return formatter.format(date)
    }

    fun getFileType(): String {
        return when {
            isDirectory -> "Папка"
            extension.isEmpty() -> "Файл"
            else -> extension.uppercase()
        }
    }

    companion object {
        private fun checkIfSystemFile(file: File): Boolean {
            val systemPaths = listOf("/system", "/proc", "/data")
            return systemPaths.any { file.absolutePath.startsWith(it) }
        }

        fun fromFile(file: File): FileItem {
            val name = file.name
            val extension = if (file.isFile && name.contains(".")) {
                name.substringAfterLast(".", "")
            } else {
                ""
            }

            return FileItem(
                name = name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0,
                lastModified = file.lastModified(),
                extension = extension,
                isHidden = file.isHidden,
                isSystemFile = checkIfSystemFile(file)
            )
        }
    }
}
