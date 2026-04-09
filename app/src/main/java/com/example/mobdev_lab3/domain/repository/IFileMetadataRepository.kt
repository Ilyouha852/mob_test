package com.example.mobdev_lab3.domain.repository

import com.example.mobdev_lab3.database.entity.FileMetadata

interface IFileMetadataRepository {
    fun getMetadataByPath(path: String): FileMetadata?
    fun getAllMetadata(): List<FileMetadata>
    fun createMetadata(metadata: FileMetadata): Long
    fun updateMetadata(metadata: FileMetadata)
    fun deleteMetadata(id: Long)
    fun addTagToFile(fileId: Long, tagId: Long)
    fun removeTagFromFile(fileId: Long, tagId: Long)
    fun getFilesByTag(tagId: Long): List<FileMetadata>
}
