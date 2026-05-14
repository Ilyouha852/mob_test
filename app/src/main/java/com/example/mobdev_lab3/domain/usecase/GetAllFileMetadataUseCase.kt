package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.data.database.entity.FileMetadata
import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

import javax.inject.Inject

class GetAllFileMetadataUseCase @Inject constructor(private val repository: IFileMetadataRepository) {
    operator fun invoke(): List<FileMetadata> = repository.getAllMetadata()
}