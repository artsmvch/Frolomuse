package com.frolo.muse.ui.main.player.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.network.NetworkHelper
import com.frolo.muse.repository.LyricsLocalRepository
import com.frolo.muse.repository.LyricsRemoteRepository
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class LyricsVMFactory constructor(
    appComponent: AppComponent,
    private val song: Song
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var networkHelper: NetworkHelper
    @Inject
    internal lateinit var localRepository: LyricsLocalRepository
    @Inject
    internal lateinit var remoteRepository: LyricsRemoteRepository
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LyricsViewModel(
            schedulerProvider = schedulerProvider,
            networkHelper = networkHelper,
            localRepository = localRepository,
            remoteRepository = remoteRepository,
            eventLogger = eventLogger,
            songArg = song
        ) as T
    }

}