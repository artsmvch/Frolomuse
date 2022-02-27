package com.frolo.muse.ui.main.library.playlists.addmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ActivityComponentInjector
import com.frolo.muse.di.ActivityComponentProvider
import com.frolo.muse.router.AppRouter
import com.frolo.muse.interactor.media.AddMediaToPlaylistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Media
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AddMediaToPlaylistVMFactory constructor(
    injector: ActivityComponentInjector,
    provider: ActivityComponentProvider,
    itemsArg: List<Media>
): ViewModelProvider.Factory {

    /*assisted inject*/
    internal lateinit var addMediaToPlaylistUseCase: AddMediaToPlaylistUseCase
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var appRouter: AppRouter
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
        addMediaToPlaylistUseCase = provider
            .provideAddMediaToPlaylistUseCaseFactory()
            .create(itemsArg)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AddMediaToPlaylistViewModel(
            addMediaToPlaylistUseCase,
            schedulerProvider,
            appRouter,
            eventLogger
        ) as T
    }

}