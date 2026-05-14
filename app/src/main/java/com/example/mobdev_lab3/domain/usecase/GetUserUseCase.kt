package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IUserRepository
import com.example.mobdev_lab3.data.network.NetworkResult
import com.example.mobdev_lab3.data.network.model.User

import javax.inject.Inject

class GetUserUseCase @Inject constructor(private val repository: IUserRepository) {
    suspend operator fun invoke(userId: Int): NetworkResult<User> =
        repository.getUser(userId)
}