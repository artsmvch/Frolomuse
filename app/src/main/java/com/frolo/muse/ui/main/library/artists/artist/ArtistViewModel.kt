package com.frolo.muse.ui.main.library.artists.artist

import androidx.lifecycle.LiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.interactor.media.shortcut.CreateShortcutUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logShortcutCreated
import com.frolo.music.model.Artist
import com.frolo.music.model.Media
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel


class ArtistViewModel constructor(
    private val createArtistShortcutUseCase: CreateShortcutUseCase<Artist>,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger,
    private val artistArg: Artist
): BaseViewModel(eventLogger) {

    private val _confirmArtistShortcutCreationEvent = SingleLiveEvent<Artist>()
    val confirmArtistShortcutCreationEvent: LiveData<Artist>
        get() = _confirmArtistShortcutCreationEvent

    fun onCreateArtistShortcutActionSelected() {
        _confirmArtistShortcutCreationEvent.value = artistArg
    }

    fun onCreateArtistShortcutActionConfirmed() {
        createArtistShortcutUseCase.createShortcut(artistArg)
                .observeOn(schedulerProvider.main())
                .doOnComplete { eventLogger.logShortcutCreated(Media.ARTIST) }
                .subscribeFor { }
    }

}