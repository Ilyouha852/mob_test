package com.example.mobdev_lab3.di

import android.content.Context
import com.example.mobdev_lab3.data.database.DatabaseManager
import com.example.mobdev_lab3.data.database.dao.DaoSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

 @Provides
 @Singleton
 fun provideDaoSession(@ApplicationContext context: Context): DaoSession {
 DatabaseManager.init(context)
 return DatabaseManager.getDaoSession()
 }
}