package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.Tag
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository
import com.example.mobdev_lab3.domain.repository.ITagRepository

class AssignTagsToFileUseCase(
    private val metadataRepository: IFileMetadataRepository,
    private val tagRepository: ITagRepository
) {
    operator fun invoke(fileId: Long, newTags: List<Tag>) {
        tagRepository.getTagsByFile(fileId).forEach { tag ->
            metadataRepository.removeTagFromFile(fileId, tag.id)
        }
        newTags.forEach { tag ->
            metadataRepository.addTagToFile(fileId, tag.id)
        }
    }
}
