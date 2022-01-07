package com.frolo.muse.ui.main.library.playlists.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logPlaylistCreated
import com.frolo.music.model.Playlist
import com.frolo.music.model.Song
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.repository.SongRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Single


class CreatePlaylistViewModel constructor(
    private val schedulerProvider: SchedulerProvider,
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    private val eventLogger: EventLogger,
    private val songsToAdd: List<Song>
): BaseViewModel(eventLogger) {

    private val _creationError: MutableLiveData<Throwable> = MutableLiveData()
    val creationError: LiveData<Throwable> = _creationError

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _playlistCreatedEvent: MutableLiveData<Playlist> = SingleLiveEvent()
    val playlistCreatedEvent: LiveData<Playlist> = _playlistCreatedEvent

    fun onSaveButtonClicked(typedName: String) {
        playlistRepository.create(typedName)
            .flatMap { playlist ->
                songRepository
                    .addToPlaylist(playlist, songsToAdd)
                    .andThen(Single.just(playlist))
            }
            .subscribeOn(schedulerProvider.worker())
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { _isLoading.value = true }
            .doFinally { _isLoading.value = false }
            .doOnSuccess { eventLogger.logPlaylistCreated(initialSongCount = songsToAdd.count()) }
            .subscribe(
                { playlist ->
                    _playlistCreatedEvent.value = playlist
                },
                { err ->
                    _creationError.value = err
                })
            .save()
    }

}