package com.frolo.muse.ui.main.library.playlists.addmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.AppComponent
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.AddMediaToPlaylistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Media
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class AddMediaToPlaylistVMFactory constructor(
        appComponent: AppComponent,
        itemsArg: List<Media>
): ViewModelProvider.Factory {

    /*assisted inject*/
    internal lateinit var addMediaToPlaylistUseCase: AddMediaToPlaylistUseCase
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var navigator: Navigator
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        appComponent.inject(this)
        addMediaToPlaylistUseCase = appComponent
                .provideAddMediaToPlaylistUseCaseFactory()
                .create(itemsArg)
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AddMediaToPlaylistViewModel(
                addMediaToPlaylistUseCase,
                schedulerProvider,
                navigator,
                eventLogger
        ) as T
    }

}