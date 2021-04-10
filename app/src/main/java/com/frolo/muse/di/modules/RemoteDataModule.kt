package com.frolo.muse.di.modules

import com.frolo.muse.di.Repo
import com.frolo.muse.di.impl.remote.FirebaseRemoteRepositoryImpl
import com.frolo.muse.repository.FirebaseRemoteRepository
import com.frolo.muse.repository.LyricsRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class RemoteDataModule {
    @Singleton
    @Provides
    @Repo(Repo.Source.REMOTE)
    fun provideLyricsRepository(): LyricsRepository {
        val remoteRepository = com.frolo.muse.di.impl.remote.LyricsRepositoryImpl()
        return com.frolo.muse.di.impl.cache.LyricsRepositoryImpl(remoteRepository, 100)
    }

    @Singleton
    @Provides
    fun provideFirebaseRemoteRepository(): FirebaseRemoteRepository {
        return FirebaseRemoteRepositoryImpl()
    }
}