package com.frolo.muse.di.modules

import com.frolo.audiofx.AudioFx
import com.frolo.player.Player
import com.frolo.player.PlayerJournal
import com.frolo.muse.engine.journals.AndroidLogPlayerJournal
import com.frolo.muse.engine.journals.CompositePlayerJournal
import com.frolo.muse.engine.journals.StoredInMemoryPlayerJournal
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class PlayerModule(
    private val player: Player,
    private val debug: Boolean
) {

    @Provides
    fun providePlayer(): Player = player

    @Provides
    fun provideAudioFx(): AudioFx = player.getAudioFx()

    @Singleton
    @Provides
    fun providePlayerJournal(): PlayerJournal {
        if (debug) {
            val journals = listOf(
                AndroidLogPlayerJournal("FrolomusePlayerJournal"),
                StoredInMemoryPlayerJournal()
            )
            return CompositePlayerJournal(journals)
        }

        return PlayerJournal.EMPTY
    }
}