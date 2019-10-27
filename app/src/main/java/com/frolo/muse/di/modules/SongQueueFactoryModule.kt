package com.frolo.muse.di.modules

import com.frolo.muse.di.impl.engine.DefaultSongQueueFactory
import com.frolo.muse.engine.SongQueueFactory
import dagger.Module
import dagger.Provides


@Module
class SongQueueFactoryModule {

    @Provides
    fun provideSongQueueFactory() : SongQueueFactory {
        return DefaultSongQueueFactory()
    }

}