package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

class GetAllFileMetadataUseCase(private val repository: IFileMetadataRepository) {
    operator fun invoke(): List<FileMetadata> = repository.getAllMetadata()
}
