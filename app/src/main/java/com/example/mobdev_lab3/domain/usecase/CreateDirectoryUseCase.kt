package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

import javax.inject.Inject

class CreateDirectoryUseCase @Inject constructor(private val repository: IFileOperationsRepository) {
    operator fun invoke(parentPath: String, name: String): Boolean =
        repository.createDirectory(parentPath, name)
}