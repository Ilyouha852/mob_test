package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.data.database.entity.Tag
import com.example.mobdev_lab3.domain.repository.ITagRepository

import javax.inject.Inject

class GetAllTagsUseCase @Inject constructor(private val repository: ITagRepository) {
    operator fun invoke(): List<Tag> = repository.getAllTags()
}