package com.frolo.muse.di.modules

import com.frolo.muse.navigator.Navigator
import dagger.Module
import dagger.Provides

@Module
class NavigationModule(private val navigator: Navigator) {

    @Provides
    fun provideNavigator(): Navigator {
        return navigator
    }

}