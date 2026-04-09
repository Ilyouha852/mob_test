package com.example.mobdev_lab3.presentation.network

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mobdev_lab3.data.repository.UserRepositoryImpl
import com.example.mobdev_lab3.domain.usecase.GetUserPostsUseCase
import com.example.mobdev_lab3.domain.usecase.GetUserUseCase
import com.example.mobdev_lab3.network.NetworkResult
import com.example.mobdev_lab3.network.model.Post
import com.example.mobdev_lab3.network.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed class NetworkUiState {
    object Idle : NetworkUiState()
    object Loading : NetworkUiState()
    data class UserLoaded(val user: User) : NetworkUiState()
    data class PostsLoaded(val posts: List<Post>, val userName: String) : NetworkUiState()
    data class Error(val message: String) : NetworkUiState()
    object Cancelled : NetworkUiState()
}

class NetworkViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepositoryImpl(application)
    private val getUserUseCase = GetUserUseCase(userRepository)
    private val getUserPostsUseCase = GetUserPostsUseCase(userRepository)

    private val _uiState = MutableLiveData<NetworkUiState>(NetworkUiState.Idle)
    val uiState: LiveData<NetworkUiState> = _uiState

    private val _lastUser = MutableLiveData<User?>(null)
    val lastUser: LiveData<User?> = _lastUser

    private var currentJob: Job? = null

    fun fetchUser(userId: Int) {
        currentJob?.cancel()
        _uiState.value = NetworkUiState.Loading
        currentJob = viewModelScope.launch {
            when (val result = getUserUseCase(userId)) {
                is NetworkResult.Success -> {
                    _lastUser.value = result.data
                    _uiState.value = NetworkUiState.UserLoaded(result.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = NetworkUiState.Error(result.message)
                }
                is NetworkResult.Cancelled -> {
                    _uiState.value = NetworkUiState.Cancelled
                }
            }
        }
    }

    fun fetchUserPosts() {
        val user = _lastUser.value ?: return
        currentJob?.cancel()
        _uiState.value = NetworkUiState.Loading
        currentJob = viewModelScope.launch {
            when (val result = getUserPostsUseCase(user.id)) {
                is NetworkResult.Success -> {
                    _uiState.value = NetworkUiState.PostsLoaded(result.data, user.name)
                }
                is NetworkResult.Error -> {
                    _uiState.value = NetworkUiState.Error(result.message)
                }
                is NetworkResult.Cancelled -> {
                    _uiState.value = NetworkUiState.Cancelled
                }
            }
        }
    }

    fun cancelRequest() {
        currentJob?.cancel()
        currentJob = null
        _uiState.value = NetworkUiState.Cancelled
    }

    fun reset() {
        cancelRequest()
        _lastUser.value = null
        _uiState.value = NetworkUiState.Idle
    }
}
