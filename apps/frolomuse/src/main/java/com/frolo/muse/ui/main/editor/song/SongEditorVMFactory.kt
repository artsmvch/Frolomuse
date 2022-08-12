package com.frolo.muse.ui.main.editor.song

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ActivityComponentInjector
import com.frolo.player.Player
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Song
import com.frolo.music.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class SongEditorVMFactory constructor(
    injector: ActivityComponentInjector,
    private val song: Song
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var application: Application
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

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SongEditorViewModel(
            application,
            player,
            schedulerProvider,
            repository,
            eventLogger,
            song
        ) as T
    }

}