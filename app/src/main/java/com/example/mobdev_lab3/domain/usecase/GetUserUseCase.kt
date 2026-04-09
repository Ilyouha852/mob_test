package com.example.mobdev_lab3.domain.usecase

import com.example.mobdev_lab3.domain.repository.IUserRepository
import com.example.mobdev_lab3.network.NetworkResult
import com.example.mobdev_lab3.network.model.User

class GetUserUseCase(private val repository: IUserRepository) {
    suspend operator fun invoke(userId: Int): NetworkResult<User> =
        repository.getUser(userId)
}
