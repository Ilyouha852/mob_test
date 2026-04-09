package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

class GetFileMetadataByPathUseCase(private val repository: IFileMetadataRepository) {
    operator fun invoke(path: String): FileMetadata? = repository.getMetadataByPath(path)
}
