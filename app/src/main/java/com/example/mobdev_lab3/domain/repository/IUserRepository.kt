package com.example.mobdev_lab3.domain.repository

import com.example.mobdev_lab3.network.NetworkResult
import com.example.mobdev_lab3.network.model.Post
import com.example.mobdev_lab3.network.model.User

interface IUserRepository {
    suspend fun getUser(id: Int): NetworkResult<User>
    suspend fun getUserPosts(userId: Int): NetworkResult<List<Post>>
}
