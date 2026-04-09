package com.example.mobdev_lab3.network

import android.content.Context
import com.example.mobdev_lab3.network.model.Post
import com.example.mobdev_lab3.network.model.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Cancelled : NetworkResult<Nothing>()
}

class NetworkRepository(private val context: Context) {

    private val api: ApiService by lazy { NetworkClient.getApiService(context) }

    suspend fun fetchUser(userId: Int): NetworkResult<User> = withContext(Dispatchers.IO) {
        try {
            val user = api.getUser(userId)
            NetworkResult.Success(user)
        } catch (e: CancellationException) {
            NetworkResult.Cancelled
        } catch (e: retrofit2.HttpException) {
            NetworkResult.Error("HTTP ${e.code()}: ${e.message()}", e.code())
        } catch (e: java.net.SocketTimeoutException) {
            NetworkResult.Error("Превышено время ожидания (таймаут)")
        } catch (e: java.io.IOException) {
            NetworkResult.Error("Ошибка сети: ${e.localizedMessage}")
        } catch (e: Exception) {
            NetworkResult.Error("Неизвестная ошибка: ${e.localizedMessage}")
        }
    }

    suspend fun fetchUserPosts(userId: Int): NetworkResult<List<Post>> = withContext(Dispatchers.IO) {
        try {
            val posts = api.getUserPosts(userId)
            NetworkResult.Success(posts)
        } catch (e: CancellationException) {
            NetworkResult.Cancelled
        } catch (e: retrofit2.HttpException) {
            NetworkResult.Error("HTTP ${e.code()}: ${e.message()}", e.code())
        } catch (e: java.net.SocketTimeoutException) {
            NetworkResult.Error("Превышено время ожидания (таймаут)")
        } catch (e: java.io.IOException) {
            NetworkResult.Error("Ошибка сети: ${e.localizedMessage}")
        } catch (e: Exception) {
            NetworkResult.Error("Неизвестная ошибка: ${e.localizedMessage}")
        }
    }
}
