package com.frolo.muse.di.modules

import android.app.Application
import android.content.Context
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.di.impl.rx.SchedulerProviderImpl
import com.frolo.muse.rx.SchedulerProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class AppModule(private val frolomuseApp: FrolomuseApp) {

    @Provides
    fun provideFrolomuseApp(): FrolomuseApp = frolomuseApp

    @Provides
    fun provideApplication(): Application = frolomuseApp

    @Provides
    fun provideContext(): Context = frolomuseApp

    @Singleton
    @Provides
    fun provideSchedulers(): SchedulerProvider {
        return SchedulerProviderImpl()
    }

}