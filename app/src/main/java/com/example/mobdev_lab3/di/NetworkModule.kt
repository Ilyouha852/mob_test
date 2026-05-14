package com.example.mobdev_lab3.di

import android.content.Context
import com.example.mobdev_lab3.data.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

 private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
 private const val CACHE_SIZE_BYTES =10L *1024 *1024
 private const val CONNECT_TIMEOUT_SEC =15L
 private const val READ_TIMEOUT_SEC =20L
 private const val WRITE_TIMEOUT_SEC =20L

 @Provides
 @Singleton
 fun provideCache(@ApplicationContext context: Context): Cache {
 val cacheDir = File(context.cacheDir, "http_cache")
 return Cache(cacheDir, CACHE_SIZE_BYTES)
 }

 @Provides
 @Singleton
 fun provideOkHttpClient(cache: Cache): OkHttpClient {
 val loggingInterceptor = HttpLoggingInterceptor().apply {
 level = HttpLoggingInterceptor.Level.HEADERS
 }

 return OkHttpClient.Builder()
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
 }

 @Provides
 @Singleton
 fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
 return Retrofit.Builder()
 .baseUrl(BASE_URL)
 .client(okHttpClient)
 .addConverterFactory(GsonConverterFactory.create())
 .build()
 }

 @Provides
 @Singleton
 fun provideApiService(retrofit: Retrofit): ApiService {
 return retrofit.create(ApiService::class.java)
 }
}
