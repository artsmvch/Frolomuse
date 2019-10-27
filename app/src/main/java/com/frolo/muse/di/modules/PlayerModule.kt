package com.frolo.muse.di.modules

import com.frolo.muse.engine.AudioFx
import com.frolo.muse.engine.Player
import dagger.Module
import dagger.Provides


@Module
class PlayerModule(private val player: Player) {

    @Provides
    fun providePlayer(): Player = player

    @Provides
    fun provideAudioFx(): AudioFx = player.getAudioFx()
}