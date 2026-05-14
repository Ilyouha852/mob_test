package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.data.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

import javax.inject.Inject

class GetFileMetadataByPathUseCase @Inject constructor(private val repository: IFileMetadataRepository) {
    operator fun invoke(path: String): FileMetadata? = repository.getMetadataByPath(path)
}