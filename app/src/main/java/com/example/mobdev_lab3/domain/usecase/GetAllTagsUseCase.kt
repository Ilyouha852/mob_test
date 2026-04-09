package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.domain.repository.ITagRepository

class GetAllTagsUseCase(private val repository: ITagRepository) {
    operator fun invoke(): List<Tag> = repository.getAllTags()
}
