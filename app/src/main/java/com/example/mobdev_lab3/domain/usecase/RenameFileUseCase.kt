package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

import javax.inject.Inject

class RenameFileUseCase @Inject constructor(private val repository: IFileOperationsRepository) {
    operator fun invoke(oldPath: String, newName: String): String? =
        repository.renameFile(oldPath, newName)
}