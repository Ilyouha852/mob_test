package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.domain.repository.ITagRepository

class GetTagsByFileUseCase(private val repository: ITagRepository) {
    operator fun invoke(fileId: Long): List<Tag> = repository.getTagsByFile(fileId)
}
