package com.frolo.muse.di.modules

import com.frolo.muse.engine.AudioFx
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.PlayerJournal
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