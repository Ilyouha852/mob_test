package com.example.mobdev_lab3.domain.repository

import com.example.mobdev_lab3.database.entity.Tag

interface ITagRepository {
    fun getAllTags(): List<Tag>
    fun getTagsByFile(fileId: Long): List<Tag>
    fun createTag(tag: Tag): Long
}
