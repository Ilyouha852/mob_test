package com.example.mobdev_lab3.data.repository

import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.database.repository.FileMetadataRepository
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

class FileMetadataRepositoryImpl : IFileMetadataRepository {

    private val dao = FileMetadataRepository()

    override fun getMetadataByPath(path: String): FileMetadata? =
        dao.getMetadataByPath(path)

    override fun getAllMetadata(): List<FileMetadata> =
        dao.getAllMetadata()

    override fun createMetadata(metadata: FileMetadata): Long =
        dao.createFileMetadata(metadata)

    override fun updateMetadata(metadata: FileMetadata) =
        dao.updateFileMetadata(metadata)

    override fun deleteMetadata(id: Long) =
        dao.deleteFileMetadata(id)

    override fun addTagToFile(fileId: Long, tagId: Long) =
        dao.addTagToFile(fileId, tagId)

    override fun removeTagFromFile(fileId: Long, tagId: Long) =
        dao.removeTagFromFile(fileId, tagId)

    override fun getFilesByTag(tagId: Long): List<FileMetadata> =
        dao.getFilesByTag(tagId)
}
