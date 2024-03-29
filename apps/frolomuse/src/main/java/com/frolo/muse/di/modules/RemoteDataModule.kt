package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.di.impl.remote.FirebaseRemoteConfigRepositoryImpl
import com.frolo.muse.di.impl.remote.FirebaseRemoteRepositoryImpl
import com.frolo.muse.di.impl.remote.lyrics.LyricsRemoteRepositoryImpl
import com.frolo.muse.repository.FirebaseRemoteRepository
import com.frolo.muse.repository.LyricsRemoteRepository
import com.frolo.muse.repository.RemoteConfigRepository
import dagger.Module
import dagger.Provides


@Module
class RemoteDataModule {
    @ApplicationScope
    @Provides
    fun provideLyricsRemoteRepository(): LyricsRemoteRepository {
        return LyricsRemoteRepositoryImpl.withCache()
    }

    @ApplicationScope
    @Provides
    fun provideFirebaseRemoteConfigRepository(context: Context): RemoteConfigRepository {
        return FirebaseRemoteConfigRepositoryImpl(context)
    }

    @ApplicationScope
    @Provides
    fun provideFirebaseRemoteRepository(): FirebaseRemoteRepository {
        return FirebaseRemoteRepositoryImpl()
    }
}