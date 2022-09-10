package com.frolo.muse.di.modules

import android.app.Application
import android.content.Context
import com.frolo.audiofx2.AudioFx2
import com.frolo.audiofx2.impl.AudioFx2Impl
import com.frolo.muse.BuildInfo
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.memory.MemoryWatcherRegistry
import com.frolo.muse.memory.MemoryWatcherRegistryStub
import com.frolo.muse.player.PlayerWrapper
import com.frolo.muse.router.AppRouter
import dagger.Module
import dagger.Provides


@Module
class ApplicationModule(private val frolomuseApp: FrolomuseApp) {

    @Provides
    fun provideFrolomuseApp(): FrolomuseApp = frolomuseApp

    @Provides
    fun provideApplication(): Application = frolomuseApp

    @Provides
    fun provideContext(): Context = frolomuseApp

    @ApplicationScope
    @Provides
    fun providePlayerWrapper(): PlayerWrapper = PlayerWrapper(BuildInfo.isDebug())

    @ApplicationScope
    @Provides
    fun provideAppRouter(): AppRouter {
        return AppRouter()
    }

    @ApplicationScope
    @Provides
    fun provideMemoryObserverRegistry(application: Application): MemoryWatcherRegistry {
        //return MemoryObserverRegistryImpl(application).apply { activate() }
        return MemoryWatcherRegistryStub
    }

    @ApplicationScope
    @Provides
    fun provideAudioFx2Impl(): AudioFx2Impl {
        return AudioFx2Impl.obtain(frolomuseApp)
    }

    @Provides
    fun provideAudioFx2(impl: AudioFx2Impl): AudioFx2 = impl
}