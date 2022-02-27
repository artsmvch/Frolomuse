package com.frolo.muse.di.modules

import android.app.Application
import android.content.Context
import com.frolo.muse.FrolomuseApp
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

}