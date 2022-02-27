package com.frolo.muse.di.modules

import com.frolo.audiofx.AudioFx
import com.frolo.muse.router.AppRouter
import com.frolo.player.Player
import dagger.Module
import dagger.Provides


@Module
class ActivityModule constructor(
    private val player: Player,
    private val router: AppRouter
) {

    @Provides
    fun providePlayer(): Player = player

    @Provides
    fun provideAudioFx(): AudioFx = player.getAudioFx()

    @Provides
    fun provideRouter(): AppRouter = router
}