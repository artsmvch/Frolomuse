package com.frolo.muse.ui.main.library.playlists.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class CreatePlaylistVMFactory constructor(
        appComponent: AppComponent,
        private val songsToAdd: List<Song>? = null
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var playlistRepository: PlaylistRepository
    @Inject
    internal lateinit var songRepository: SongRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CreatePlaylistViewModel(
                schedulerProvider,
                playlistRepository,
                songRepository,
                eventLogger,
                songsToAdd ?: emptyList()
        ) as T
    }

}