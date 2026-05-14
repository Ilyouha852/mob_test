package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IFileOperationsRepository

import javax.inject.Inject

class GetFileContentUseCase @Inject constructor(private val repository: IFileOperationsRepository) {
    operator fun invoke(path: String, isDirectory: Boolean): String =
        if (isDirectory) repository.getDirectoryContents(path)
        else repository.getFileContent(path)
}