package com.example.mobdev_lab3.database.repository

import com.example.mobdev_lab3.database.DatabaseManager
import com.example.mobdev_lab3.database.dao.FileMetadataDao
import com.example.mobdev_lab3.database.dao.FileMetadataTagDao
import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.database.entity.FileMetadataTag
import com.example.mobdev_lab3.database.entity.Tag

class FileMetadataRepository {

    private val fileMetadataDao: FileMetadataDao
        get() = DatabaseManager.getDaoSession().fileMetadataDao

    private val fileMetadataTagDao: FileMetadataTagDao
        get() = DatabaseManager.getDaoSession().fileMetadataTagDao

    fun createFileMetadata(metadata: FileMetadata): Long {
        return fileMetadataDao.insert(metadata)
    }

    fun getMetadataByPath(path: String): FileMetadata? {
        return fileMetadataDao.queryBuilder()
            .where(FileMetadataDao.Properties.FilePath.eq(path))
            .unique()
    }

    fun getAllMetadata(): List<FileMetadata> {
        return fileMetadataDao.loadAll()
    }

    fun updateFileMetadata(metadata: FileMetadata) {
        fileMetadataDao.update(metadata)
    }

    fun deleteFileMetadata(id: Long) {
        fileMetadataDao.deleteByKey(id)
    }

    fun addTagToFile(fileId: Long, tagId: Long) {
        // Проверяем, существует ли уже такая связь
        val existingLink = fileMetadataTagDao.queryBuilder()
            .where(FileMetadataTagDao.Properties.FileMetadataId.eq(fileId),
                   FileMetadataTagDao.Properties.TagId.eq(tagId))
            .unique()

        if (existingLink == null) {
            val link = FileMetadataTag()
            link.fileMetadataId = fileId
            link.tagId = tagId
            fileMetadataTagDao.insert(link)
        }
    }

    fun removeTagFromFile(fileId: Long, tagId: Long) {
        val link = fileMetadataTagDao.queryBuilder()
            .where(FileMetadataTagDao.Properties.FileMetadataId.eq(fileId),
                   FileMetadataTagDao.Properties.TagId.eq(tagId))
            .unique()

        if (link != null) {
            fileMetadataTagDao.delete(link)
        }
    }

    fun getFilesByTag(tagId: Long): List<FileMetadata> {
        val links = fileMetadataTagDao.queryBuilder()
            .where(FileMetadataTagDao.Properties.TagId.eq(tagId))
            .list()
        
        val fileIds = links.map { it.fileMetadataId }
        
        return fileMetadataDao.queryBuilder()
            .where(FileMetadataDao.Properties.Id.`in`(fileIds))
            .list()
    }

    fun getFavoriteFiles(): List<FileMetadata> {
        return fileMetadataDao.queryBuilder()
            .where(FileMetadataDao.Properties.IsFavorite.eq(true))
            .list()
    }
}
