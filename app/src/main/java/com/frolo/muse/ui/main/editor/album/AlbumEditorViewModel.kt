package com.frolo.muse.ui.main.editor.album

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Album
import com.frolo.muse.model.media.AlbumArtConfig
import com.frolo.muse.repository.AlbumRepository
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Single


class AlbumEditorViewModel constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: AlbumRepository,
        private val eventLogger: EventLogger,
        private val albumArg: Album
): BaseViewModel(eventLogger) {

    data class UpdateEvent constructor(
            val album: Album,
            val artChanged: Boolean,
            val newFilepath: String?)

    //
    private var _activated: Boolean = false

    private val _selectedFilepath: MutableLiveData<String> = MutableLiveData()

    // True if art was changed by selecting a new image or deleting the current one.
    // False - if art wasn't changed.
    private var _artWasChanged = false

    private val _albumArtConfig: MutableLiveData<AlbumArtConfig> =
            MediatorLiveData<AlbumArtConfig>().apply {
                addSource(_selectedFilepath) { filepath ->
                    value = AlbumArtConfig(null, filepath)
                }
    }
    val albumArtConfig: LiveData<AlbumArtConfig> get() = _albumArtConfig

    private val _isLoadingUpdate: MutableLiveData<Boolean> = MutableLiveData()
    val isLoadingUpdate: LiveData<Boolean> = _isLoadingUpdate

    private val _updatedEvent: MutableLiveData<UpdateEvent> = MutableLiveData()
    val updatedEvent: LiveData<UpdateEvent> = _updatedEvent

    fun onActive() {
        if (!_activated) {
            _activated = true
            _artWasChanged = false
            _albumArtConfig.value = AlbumArtConfig(albumArg.id, null)
        }
    }

    fun onFileSelected(filepath: String?) {
        _artWasChanged = true
        _selectedFilepath.value = filepath
    }

    fun onDeleteClicked() {
        _artWasChanged = true
        _selectedFilepath.value = null
    }

    fun onSaveClicked() {
        val operator = if (_artWasChanged) {
            // Update the art ONLY if artWasChanged == true
            val filepath = _selectedFilepath.value

            repository
                    .updateArt(albumArg.id, filepath)
                    .andThen(repository.getItem(albumArg.id))
                    .firstOrError()
                    .map { album ->
                        UpdateEvent(album, true, filepath)
                    }
        } else {
            Single.just(albumArg)
                    .map { album ->
                        UpdateEvent(album, false, null)
                    }
        }

        operator
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.main())
                .doOnSuccess { _isLoadingUpdate.value = true }
                .doFinally { _isLoadingUpdate.value = false }
                .subscribeFor { event ->
                    _updatedEvent.value = event
                }
    }
}