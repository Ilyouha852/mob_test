package com.example.mobdev_lab3.database.repository

import com.example.mobdev_lab3.database.DatabaseManager
import com.example.mobdev_lab3.database.dao.FileMetadataTagDao
import com.example.mobdev_lab3.database.dao.TagDao
import com.example.mobdev_lab3.database.entity.Tag

class TagRepository {

    private val tagDao: TagDao
        get() = DatabaseManager.getDaoSession().tagDao
        
    private val fileMetadataTagDao: FileMetadataTagDao
        get() = DatabaseManager.getDaoSession().fileMetadataTagDao

    fun createTag(tag: Tag): Long {
        return tagDao.insert(tag)
    }

    fun getTagById(id: Long): Tag? {
        return tagDao.load(id)
    }

    fun getAllTags(): List<Tag> {
        return tagDao.loadAll()
    }

    fun updateTag(tag: Tag) {
        tagDao.update(tag)
    }

    fun deleteTag(id: Long) {
        // Сначала удаляем связи
        val links = fileMetadataTagDao.queryBuilder()
            .where(FileMetadataTagDao.Properties.TagId.eq(id))
            .list()
        
        for (link in links) {
            fileMetadataTagDao.delete(link)
        }
        
        // Затем удаляем сам тег
        tagDao.deleteByKey(id)
    }
    
    fun getTagsByFile(fileId: Long): List<Tag> {
        val links = fileMetadataTagDao.queryBuilder()
            .where(FileMetadataTagDao.Properties.FileMetadataId.eq(fileId))
            .list()
            
        val tagIds = links.map { it.tagId }
        
        return tagDao.queryBuilder()
            .where(TagDao.Properties.Id.`in`(tagIds))
            .list()
    }
}
