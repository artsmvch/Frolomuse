package com.frolo.muse.ui.main.library.playlists.addmedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.arch.support.combine
import com.frolo.arch.support.map
import com.frolo.muse.interactor.media.AddMediaToPlaylistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logMediaAddedToPlaylist
import com.frolo.music.model.Playlist
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers


class AddMediaToPlaylistViewModel constructor(
    private val addMediaToPlaylistUseCase: AddMediaToPlaylistUseCase,
    private val schedulerProvider: SchedulerProvider,
    private val eventLogger: EventLogger
) : BaseViewModel(eventLogger) {

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _playlists: MutableLiveData<List<Playlist>> by lazy {
        MutableLiveData<List<Playlist>>().apply {
            addMediaToPlaylistUseCase.getPlaylists()
                    .observeOn(schedulerProvider.main())
                    .doOnSubscribe { _isLoading.value = true }
                    .doOnNext { _isLoading.value = false }
                    .subscribeFor { list -> value = list }
        }
    }
    val playlists: LiveData<List<Playlist>> get() = _playlists

    val placeholderVisible: LiveData<Boolean> = combine(playlists, isLoading) { list, isLoading ->
        list.isNullOrEmpty() && isLoading != true
    }

    private val _isAddingItemsToPlaylist: MutableLiveData<Boolean> = MutableLiveData()
    val isAddingItemsToPlaylist: LiveData<Boolean> = _isAddingItemsToPlaylist

    private val _itemsAddedToPlaylistEvent: MutableLiveData<Unit> = MutableLiveData()
    val itemsAddedToPlaylistEvent: LiveData<Unit> = _itemsAddedToPlaylistEvent

    private val _checkedPlaylists = MutableLiveData<Map<Playlist.Identifier, Playlist>>()

    val isAddButtonEnabled: LiveData<Boolean> =
        _checkedPlaylists.map(initialValue = false) { map -> !map.isNullOrEmpty() }

    fun onCheckedPlaylistsChanged(checkedPlaylists: Map<Playlist.Identifier, Playlist>) {
        _checkedPlaylists.value = checkedPlaylists
    }

    fun onAddButtonClicked() {
        val checkedPlaylists = HashMap(_checkedPlaylists.value.orEmpty())
        Single.fromCallable { checkedPlaylists.values }
            .subscribeOn(Schedulers.computation())
            .flatMapCompletable { addMediaToPlaylistUseCase.addMediaToPlaylists(it) }
            .observeOn(schedulerProvider.main())
            .doOnSubscribe { _isAddingItemsToPlaylist.value = true }
            .doFinally { _isAddingItemsToPlaylist.value = false }
            .doOnComplete { eventLogger.logMediaAddedToPlaylist() }
            .subscribeFor { _itemsAddedToPlaylistEvent.value = Unit }
    }
}
