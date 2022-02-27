package com.frolo.muse.di

import com.frolo.muse.di.modules.ServiceModule
import com.frolo.muse.engine.PlayerStateRestorer
import com.frolo.muse.engine.service.PlayerBuilder
import com.frolo.muse.interactor.media.favourite.ChangeSongFavStatusUseCase
import com.frolo.muse.repository.Preferences
import dagger.Subcomponent


@ServiceScope
@Subcomponent(
    modules = [
        ServiceModule::class
    ]
)
interface ServiceComponent {
    fun providePreferences(): Preferences
    fun provideChangeSongFavStatusUseCase(): ChangeSongFavStatusUseCase
    fun providePlayerStateRestorer(): PlayerStateRestorer
    fun providePlayerBuilder(): PlayerBuilder
}