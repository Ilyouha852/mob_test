package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.data.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository
import com.example.mobdev_lab3.domain.model.FileItem
import java.util.Date

import javax.inject.Inject

class ToggleFileInDatabaseUseCase @Inject constructor(private val repository: IFileMetadataRepository) {
    operator fun invoke(file: FileItem, currentMetadata: FileMetadata?): Boolean {
        return if (currentMetadata != null) {
            repository.deleteMetadata(currentMetadata.id)
            false
        } else {
            val metadata = FileMetadata().apply {
                filePath = file.path
                fileName = file.name
                lastAccessDate = Date().time
                isFavorite = false
            }
            repository.createMetadata(metadata)
            true
        }
    }
}