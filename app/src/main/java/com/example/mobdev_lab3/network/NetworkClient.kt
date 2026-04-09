package com.example.mobdev_lab3.network

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024
    private const val CONNECT_TIMEOUT_SEC = 15L
    private const val READ_TIMEOUT_SEC    = 20L
    private const val WRITE_TIMEOUT_SEC   = 20L

    private var retrofit: Retrofit? = null

    fun init(context: Context): Retrofit {
        if (retrofit != null) return retrofit!!

        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, CACHE_SIZE_BYTES)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val okHttpClient = OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .cache(cache)
            .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=60")
                    .build()
            }
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit!!
    }

    fun getApiService(context: Context): ApiService =
        init(context).create(ApiService::class.java)

    fun getCacheInfo(context: Context): String {
        val cacheDir = File(context.cacheDir, "http_cache")
        if (!cacheDir.exists()) return "Кэш не создан"
        val sizeKb = cacheDir.walkTopDown()
            .filter { it.isFile }
            .sumOf { it.length() } / 1024
        return "Кэш: $sizeKb КБ (директория: ${cacheDir.absolutePath})"
    }
}
