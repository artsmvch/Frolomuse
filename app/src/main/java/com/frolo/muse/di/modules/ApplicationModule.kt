package com.frolo.muse.di.modules

import android.app.Application
import android.content.Context
import com.frolo.muse.ActivityWatcher
import com.frolo.muse.BuildInfo
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.di.ApplicationScope
import com.frolo.muse.engine.PlayerWrapper
import com.frolo.muse.memory.MemoryObserverRegistry
import com.frolo.muse.memory.MemoryObserverRegistryImpl
import com.frolo.muse.memory.MemoryObserverRegistryStub
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
    fun provideMemoryObserverRegistry(application: Application): MemoryObserverRegistry {
        //return MemoryObserverRegistryImpl(application).apply { activate() }
        return MemoryObserverRegistryStub
    }

}