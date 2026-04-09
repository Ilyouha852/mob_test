package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

class RenameFileUseCase(private val repository: IFileOperationsRepository) {
    operator fun invoke(oldPath: String, newName: String): String? =
        repository.renameFile(oldPath, newName)
}
