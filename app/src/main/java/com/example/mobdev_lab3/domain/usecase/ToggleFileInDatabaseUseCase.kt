package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository
import com.example.mobdev_lab3.model.FileItem
import java.util.Date

class ToggleFileInDatabaseUseCase(private val repository: IFileMetadataRepository) {
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
