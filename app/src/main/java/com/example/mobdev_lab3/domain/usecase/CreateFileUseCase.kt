package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

import javax.inject.Inject

class CreateFileUseCase @Inject constructor(private val repository: IFileOperationsRepository) {
    operator fun invoke(parentPath: String, name: String): Boolean =
        repository.createFile(parentPath, name)
}