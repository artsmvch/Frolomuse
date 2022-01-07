package com.frolo.muse.ui.main.player.poster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.interactor.poster.CreatePosterUseCase
import com.frolo.muse.interactor.poster.SavePosterUseCase
import com.frolo.muse.router.AppRouter
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Song
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject


class PosterVMFactory @AssistedInject constructor(
    private val createPosterUseCase: CreatePosterUseCase,
    private val savePosterUseCase: SavePosterUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val appRouter: AppRouter,
    private val eventLogger: EventLogger,
    @Assisted private val song: Song
): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PosterViewModel(
            createPosterUseCase,
            savePosterUseCase,
            schedulerProvider,
            appRouter,
            eventLogger,
            song
        ) as T
    }

    @AssistedInject.Factory
    interface Creator {
        fun create(song: Song): PosterVMFactory
    }

}