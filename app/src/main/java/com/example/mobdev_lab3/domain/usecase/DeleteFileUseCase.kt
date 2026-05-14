package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

import javax.inject.Inject

class DeleteFileUseCase @Inject constructor(private val repository: IFileOperationsRepository) {
    operator fun invoke(path: String): Boolean = repository.deleteFileOrDir(path)
}