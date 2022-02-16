package com.frolo.muse.ui.main.library.artists.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.frolo.muse.di.ComponentInjector
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.music.model.Artist
import com.frolo.muse.rx.SchedulerProvider
import javax.inject.Inject


class ArtistVMFactory constructor(
    injector: ComponentInjector,
    private val artist: Artist
): ViewModelProvider.Factory {

    @Inject
    internal lateinit var createArtistShortcutUseCase: CreateShortcutUseCase<Artist>
    @Inject
    internal lateinit var schedulerProvider: SchedulerProvider
    @Inject
    internal lateinit var eventLogger: EventLogger

    init {
        injector.inject(this)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ArtistViewModel(
            createArtistShortcutUseCase,
            schedulerProvider,
            eventLogger,
            artist
        ) as T
    }

}