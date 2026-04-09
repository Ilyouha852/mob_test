package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository
import java.io.File

class ListFilesInDirectoryUseCase(private val repository: IFileOperationsRepository) {
    operator fun invoke(path: String): List<File> = repository.listFiles(path)
}
