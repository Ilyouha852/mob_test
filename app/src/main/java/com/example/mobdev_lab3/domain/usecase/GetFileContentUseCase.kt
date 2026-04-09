package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

class GetFileContentUseCase(private val repository: IFileOperationsRepository) {
    operator fun invoke(path: String, isDirectory: Boolean): String =
        if (isDirectory) repository.getDirectoryContents(path)
        else repository.getFileContent(path)
}
