package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

class UpdateFileMetadataUseCase(private val repository: IFileMetadataRepository) {
    operator fun invoke(metadata: FileMetadata) = repository.updateMetadata(metadata)
}
