package com.frolo.muse.ui.main.editor.song

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.di.ComponentInjector
import com.frolo.player.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Song
import com.frolo.music.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class SongEditorVMFactory constructor(
    injector: ComponentInjector,
    private val song: Song
): ViewModelProvider.Factory {

    internal lateinit var frolomuseApp: FrolomuseApp
    @Inject
    internal lateinit var player: Player
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var repository: SongRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SongEditorViewModel(
            frolomuseApp,
            player,
            schedulerProvider,
            repository,
            eventLogger,
            song
        ) as T
    }

}