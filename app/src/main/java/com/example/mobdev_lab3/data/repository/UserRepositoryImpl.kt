package com.example.mobdev_lab3.data.repository

import android.content.Context
import com.example.mobdev_lab3.domain.repository.IUserRepository
import com.example.mobdev_lab3.network.NetworkRepository
import com.example.mobdev_lab3.network.NetworkResult
import com.example.mobdev_lab3.network.model.Post
import com.example.mobdev_lab3.network.model.User

class UserRepositoryImpl(context: Context) : IUserRepository {

    private val networkRepository = NetworkRepository(context)

    override suspend fun getUser(id: Int): NetworkResult<User> =
        networkRepository.fetchUser(id)

    override suspend fun getUserPosts(userId: Int): NetworkResult<List<Post>> =
        networkRepository.fetchUserPosts(userId)
}
