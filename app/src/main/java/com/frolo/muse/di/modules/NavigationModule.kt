package com.frolo.muse.di.modules

import com.frolo.muse.router.AppRouter
import dagger.Module
import dagger.Provides

@Module
class NavigationModule(private val appRouter: AppRouter) {

    @Provides
    fun provideNavigator(): AppRouter {
        return appRouter
    }

}