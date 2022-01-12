package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ComponentInjector
import com.frolo.muse.di.ComponentProvider
import com.frolo.muse.interactor.media.AddSongToPlaylistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Playlist
import com.frolo.muse.router.AppRouter
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AddSongToPlaylistVMFactory constructor(
    injector: ComponentInjector,
    provider: ComponentProvider,
    playlistArg: Playlist
): ViewModelProvider.Factory {

    /*assisted inject*/
    internal lateinit var addSongToPlaylistUseCase: AddSongToPlaylistUseCase
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var appRouter: AppRouter
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
        addSongToPlaylistUseCase = provider
            .provideAddSongToPlaylistUseCaseFactory()
            .create(playlistArg)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AddSongToPlaylistViewModel(
            addSongToPlaylistUseCase,
            schedulerProvider,
            appRouter,
            eventLogger
        ) as T
    }

}