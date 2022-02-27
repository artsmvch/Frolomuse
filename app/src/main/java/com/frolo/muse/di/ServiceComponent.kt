package com.frolo.muse.di

import com.frolo.muse.di.modules.ServiceModule
import com.frolo.muse.engine.service.PlayerBuilder
import com.frolo.muse.engine.service.PlayerService
import dagger.Subcomponent


@ServiceScope
@Subcomponent(
    modules = [
        ServiceModule::class
    ]
)
interface ServiceComponent {
    fun inject(service: PlayerService)

    fun providePlayerBuilder(): PlayerBuilder
}