package com.frolo.muse.di.modules

import com.frolo.audiofx.AudioFx
import com.frolo.muse.player.PlayerWrapper
import com.frolo.player.Player
import dagger.Module
import dagger.Provides


@Module
class ActivityModule {

    @Provides
    fun providePlayer(playerWrapper: PlayerWrapper): Player = playerWrapper

    @Provides
    fun provideAudioFx(player: Player): AudioFx = player.getAudioFx()
}