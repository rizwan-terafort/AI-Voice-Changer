package com.voicechanger.funnysound.di

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModules {
    @Provides
    @Singleton
    fun getContext(@ApplicationContext context: Context): Context {
        return context
    }
//
//    @Provides
//    @Singleton
//    fun getAppDatabase(@ApplicationContext context: Context): AppDatabase {
//        return AppDatabase.getAppDBInstance(context)
//    }
//
//    @Provides
//    @Singleton
//    fun getAppDao(appDatabase: AppDatabase): FramesDao {
//        return appDatabase.getAppDao()
//    }

    @Provides
    @Singleton
    fun providesCoroutineScope(
            @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)

//    @Provides
//    @Singleton
//    fun providesFireBaseRemoteConfig(): FirebaseRemoteConfig {
//        return RemoteConfigUtils.remoteConfig
//    }
}