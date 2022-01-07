package com.frolo.muse.ui.main.editor.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logPlaylistUpdated
import com.frolo.music.model.Playlist
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel


class PlaylistEditorViewModel constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: PlaylistRepository,
        private val eventLogger: EventLogger,
        private val playlistArg: Playlist
): BaseViewModel(eventLogger) {

    private val _isLoadingUpdate: MutableLiveData<Boolean> = MutableLiveData()
    val isLoadingUpdate: LiveData<Boolean> = _isLoadingUpdate

    private val _updatedPlaylist: MutableLiveData<Playlist> = MutableLiveData()
    val updatedPlaylist: LiveData<Playlist> = _updatedPlaylist

    private val _inputError: MutableLiveData<Throwable> = SingleLiveEvent()
    val inputError: LiveData<Throwable> = _inputError

    fun onSaveClicked(name: String) {
        repository.update(playlistArg, name)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isLoadingUpdate.value = true }
                .doFinally { _isLoadingUpdate.value = false }
                .doOnSuccess { eventLogger.logPlaylistUpdated() }
                .subscribe{ playlist, err ->
                    if (playlist != null) {
                        _updatedPlaylist.value = playlist
                    } else if (err != null) {
                        _inputError.value = err
                    }
                }
                .save()
    }

}