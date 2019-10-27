package com.frolo.muse.di.modules

import android.content.Context
import com.frolo.muse.App
import com.frolo.muse.di.impl.rx.SchedulerProviderImpl
import com.frolo.muse.rx.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class AppModule(private val app: App) {
    @Provides
    fun provideApp(): App = app

    @Provides
    fun provideContext(): Context = app

    @Singleton
    @Provides
    fun provideSchedulers(): SchedulerProvider {
        return SchedulerProviderImpl()
    }
}