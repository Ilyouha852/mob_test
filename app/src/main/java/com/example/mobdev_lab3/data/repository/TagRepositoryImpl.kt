package com.example.mobdev_lab3.data.repository

import com.example.mobdev_lab3.data.database.entity.Tag
import com.example.mobdev_lab3.data.database.repository.TagRepository
import com.example.mobdev_lab3.domain.repository.ITagRepository

import javax.inject.Inject

class TagRepositoryImpl @Inject constructor() : ITagRepository {

 private val dao = TagRepository()

    override fun getAllTags(): List<Tag> = dao.getAllTags()

    override fun getTagsByFile(fileId: Long): List<Tag> = dao.getTagsByFile(fileId)

    override fun createTag(tag: Tag): Long = dao.createTag(tag)
}
