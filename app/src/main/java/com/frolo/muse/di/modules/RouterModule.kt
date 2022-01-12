package com.frolo.muse.di.modules

import com.frolo.muse.router.AppRouter
import dagger.Module
import dagger.Provides


@Module
class RouterModule(private val appRouter: AppRouter) {

    @Provides
    fun provideRouter(): AppRouter {
        return appRouter
    }

}