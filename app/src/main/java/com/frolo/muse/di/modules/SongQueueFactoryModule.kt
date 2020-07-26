package com.frolo.muse.di.modules

import com.frolo.muse.di.impl.engine.DefaultAudioSourceQueueFactory
import com.frolo.muse.common.AudioSourceQueueFactory
import dagger.Module
import dagger.Provides


@Module
class SongQueueFactoryModule {

    @Provides
    fun provideSongQueueFactory() : AudioSourceQueueFactory {
        return DefaultAudioSourceQueueFactory()
    }

}