package com.frolo.muse.di.modules

import android.app.Application
import android.content.Context
import com.frolo.muse.ActivityWatcher
import com.frolo.muse.BuildInfo
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.player.PlayerWrapper
import com.frolo.muse.memory.MemoryWatcherRegistry
import com.frolo.muse.memory.MemoryWatcherRegistryStub
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

    @Provides
    fun provideActivityWatcher(): ActivityWatcher = frolomuseApp

    @ApplicationScope
    @Provides
    fun providePlayerWrapper(): PlayerWrapper = PlayerWrapper(BuildInfo.isDebug())

    @ApplicationScope
    @Provides
    fun provideAppRouter(activityWatcher: ActivityWatcher): AppRouter {
        return AppRouter(activityWatcher)
    }

    @ApplicationScope
    @Provides
    fun provideMemoryObserverRegistry(application: Application): MemoryWatcherRegistry {
        //return MemoryObserverRegistryImpl(application).apply { activate() }
        return MemoryWatcherRegistryStub
    }

}