package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

class DeleteFileUseCase(private val repository: IFileOperationsRepository) {
    operator fun invoke(path: String): Boolean = repository.deleteFileOrDir(path)
}
