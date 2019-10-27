package com.frolo.muse.ui.main.player.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.di.Repo
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Song
import com.frolo.muse.network.NetworkHelper
import com.frolo.muse.repository.LyricsRepository
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
    @field:[Repo(Repo.Source.LOCAL)]
    internal lateinit var localRepository: LyricsRepository
    @Inject
    @field:[Repo(Repo.Source.REMOTE)]
    internal lateinit var remoteRepository: LyricsRepository
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