package com.frolo.muse.di.modules

import com.frolo.muse.di.impl.remote.FirebaseRemoteConfigRepositoryImpl
import com.frolo.muse.di.impl.remote.FirebaseRemoteRepositoryImpl
import com.frolo.muse.di.impl.remote.lyrics.LyricsRemoteRepositoryImpl
import com.frolo.muse.repository.FirebaseRemoteRepository
import com.frolo.muse.repository.LyricsRemoteRepository
import com.frolo.muse.repository.RemoteConfigRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class RemoteDataModule {
    @Singleton
    @Provides
    fun provideLyricsRemoteRepository(): LyricsRemoteRepository {
        return LyricsRemoteRepositoryImpl.withCache()
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteConfigRepository(): RemoteConfigRepository {
        return FirebaseRemoteConfigRepositoryImpl()
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteRepository(): FirebaseRemoteRepository {
        return FirebaseRemoteRepositoryImpl()
    }
}