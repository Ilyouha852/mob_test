package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.domain.repository.ITagRepository

class CreateTagUseCase(private val repository: ITagRepository) {
    operator fun invoke(tag: Tag): Long = repository.createTag(tag)
}
