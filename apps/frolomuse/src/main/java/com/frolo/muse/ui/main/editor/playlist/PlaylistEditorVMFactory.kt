package com.frolo.muse.ui.main.editor.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ActivityComponentInjector
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Playlist
import com.frolo.music.repository.PlaylistRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class PlaylistEditorVMFactory constructor(
    injector: ActivityComponentInjector,
    private val playlist: Playlist
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var repository: PlaylistRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PlaylistEditorViewModel(
            schedulerProvider,
            repository,
            eventLogger,
            playlist
        ) as T
    }

}