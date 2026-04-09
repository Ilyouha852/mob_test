package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

class CreateDirectoryUseCase(private val repository: IFileOperationsRepository) {
    operator fun invoke(parentPath: String, name: String): Boolean =
        repository.createDirectory(parentPath, name)
}
