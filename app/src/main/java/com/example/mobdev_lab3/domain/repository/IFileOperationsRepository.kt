package com.example.mobdev_lab3.domain.repository

import java.io.File

interface IFileOperationsRepository {
    fun getFileContent(path: String): String
    fun getDirectoryContents(path: String): String
    fun deleteFileOrDir(path: String): Boolean
    fun renameFile(oldPath: String, newName: String): String?
    fun listFiles(path: String): List<File>
    fun createFile(parentPath: String, name: String): Boolean
    fun createDirectory(parentPath: String, name: String): Boolean
    fun getMimeType(extension: String): String
    fun getDefaultStoragePath(): String
}
