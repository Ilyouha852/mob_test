package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileMetadataRepository

import javax.inject.Inject

class DeleteFileMetadataUseCase @Inject constructor(private val repository: IFileMetadataRepository) {
    operator fun invoke(id: Long) = repository.deleteMetadata(id)
}