package com.example.mobdev_lab3.data.repository

import android.os.Environment
import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository
import java.io.File

class FileOperationsRepositoryImpl : IFileOperationsRepository {

    override fun getFileContent(path: String): String {
        return try {
            val file = File(path)
            if (!file.exists() || !file.isFile) return "Файл не найден"
            when (file.extension.lowercase()) {
                "txt", "log", "md", "csv", "xml", "json", "html", "htm", "css", "js" -> {
                    val text = file.readText(Charsets.UTF_8)
                    if (text.length > 2000) text.take(2000) + "\n\n... (показаны первые 2000 символов)"
                    else text
                }
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "Изображение (предпросмотр недоступен)"
                "mp4", "avi", "mkv", "mov", "3gp" -> "Видео файл (предпросмотр недоступен)"
                "mp3", "wav", "flac", "aac", "ogg" -> "Аудио файл (предпросмотр недоступен)"
                else -> "Бинарный файл (предпросмотр недоступен)"
            }
        } catch (e: Exception) {
            "Ошибка чтения файла: ${e.message}"
        }
    }

    override fun getDirectoryContents(path: String): String {
        return try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) return "Папка не найдена"
            val files = directory.listFiles()
            files?.joinToString("\n") { f ->
                val icon = if (f.isDirectory) "📁" else "📄"
                "$icon ${f.name}"
            } ?: "Папка пуста"
        } catch (e: Exception) {
            "Ошибка чтения папки: ${e.message}"
        }
    }

    override fun deleteFileOrDir(path: String): Boolean {
        return try {
            deleteRecursively(File(path))
        } catch (e: Exception) {
            false
        }
    }

    private fun deleteRecursively(file: File): Boolean {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        return file.delete()
    }

    override fun renameFile(oldPath: String, newName: String): String? {
        return try {
            val old = File(oldPath)
            val newFile = File(old.parent, newName)
            if (old.renameTo(newFile)) newFile.absolutePath else null
        } catch (e: Exception) {
            null
        }
    }

    override fun listFiles(path: String): List<File> {
        return try {
            val directory = File(path)
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()
                    ?.sortedWith(compareBy<File> { it.isFile }.thenBy { it.name.lowercase() })
                    ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun createFile(parentPath: String, name: String): Boolean {
        return try {
            File(parentPath, name).createNewFile()
        } catch (e: Exception) {
            false
        }
    }

    override fun createDirectory(parentPath: String, name: String): Boolean {
        return try {
            File(parentPath, name).mkdirs()
        } catch (e: Exception) {
            false
        }
    }

    override fun getMimeType(extension: String): String = when (extension.lowercase()) {
        "txt" -> "text/plain"
        "html", "htm" -> "text/html"
        "css" -> "text/css"
        "js" -> "text/javascript"
        "json" -> "application/json"
        "xml" -> "application/xml"
        "pdf" -> "application/pdf"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "gif" -> "image/gif"
        "mp4" -> "video/mp4"
        "mp3" -> "audio/mpeg"
        else -> "*/*"
    }

    override fun getDefaultStoragePath(): String {
        return try {
            val dir = Environment.getExternalStorageDirectory()
            if (dir != null && dir.exists()) dir.absolutePath else "/storage/emulated/0"
        } catch (e: Exception) {
            "/storage/emulated/0"
        }
    }
}
