package com.example.mobdev_lab3.data.repository

import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.database.repository.TagRepository
import com.example.mobdev_lab3.domain.repository.ITagRepository

class TagRepositoryImpl : ITagRepository {

    private val dao = TagRepository()

    override fun getAllTags(): List<Tag> = dao.getAllTags()

    override fun getTagsByFile(fileId: Long): List<Tag> = dao.getTagsByFile(fileId)

    override fun createTag(tag: Tag): Long = dao.createTag(tag)
}
