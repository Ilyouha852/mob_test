package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.data.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

import javax.inject.Inject

class UpdateFileMetadataUseCase @Inject constructor(private val repository: IFileMetadataRepository) {
    operator fun invoke(metadata: FileMetadata) = repository.updateMetadata(metadata)
}