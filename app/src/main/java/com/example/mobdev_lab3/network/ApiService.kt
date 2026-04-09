package com.example.mobdev_lab3.network

import com.example.mobdev_lab3.network.model.Post
import com.example.mobdev_lab3.network.model.User
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): User

    @GET("posts")
    suspend fun getUserPosts(@Query("userId") userId: Int): List<Post>
}
