package com.example.mobdev_lab3.data.repository

import android.content.Context
import com.example.mobdev_lab3.domain.repository.IUserRepository
import com.example.mobdev_lab3.data.network.NetworkRepository
import com.example.mobdev_lab3.data.network.NetworkResult
import com.example.mobdev_lab3.data.network.model.Post
import com.example.mobdev_lab3.data.network.model.User

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
 @ApplicationContext private val context: Context
) : IUserRepository {

 private val networkRepository = NetworkRepository(context)

    override suspend fun getUser(id: Int): NetworkResult<User> =
        networkRepository.fetchUser(id)

    override suspend fun getUserPosts(userId: Int): NetworkResult<List<Post>> =
        networkRepository.fetchUserPosts(userId)
}
