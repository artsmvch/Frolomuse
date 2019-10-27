package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.interactor.media.AddSongToPlaylistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AddSongToPlaylistVMFactory constructor(
        appComponent: AppComponent,
        playlistArg: Playlist
): ViewModelProvider.Factory {

    /*assisted inject*/
    internal lateinit var addSongToPlaylistUseCase: AddSongToPlaylistUseCase
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
        addSongToPlaylistUseCase = appComponent
                .provideAddSongToPlaylistUseCaseFactory()
                .create(playlistArg)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AddSongToPlaylistViewModel(
                addSongToPlaylistUseCase,
                schedulerProvider,
                eventLogger
        ) as T
    }

}