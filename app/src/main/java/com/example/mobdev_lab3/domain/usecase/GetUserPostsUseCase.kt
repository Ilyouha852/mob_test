package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IUserRepository
import com.example.mobdev_lab3.data.network.NetworkResult
import com.example.mobdev_lab3.data.network.model.Post

import javax.inject.Inject

class GetUserPostsUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(userId: Int): NetworkResult<List<Post>> =
        repository.getUserPosts(userId)
}