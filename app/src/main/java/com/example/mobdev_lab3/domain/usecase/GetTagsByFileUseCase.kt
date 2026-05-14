package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.data.database.entity.Tag
import com.example.mobdev_lab3.domain.repository.ITagRepository

import javax.inject.Inject

class GetTagsByFileUseCase @Inject constructor(private val repository: ITagRepository) {
    operator fun invoke(fileId: Long): List<Tag> = repository.getTagsByFile(fileId)
}