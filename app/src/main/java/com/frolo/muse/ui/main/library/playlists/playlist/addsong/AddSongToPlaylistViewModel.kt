package com.frolo.muse.ui.main.library.playlists.playlist.addsong

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.frolo.muse.interactor.media.AddSongToPlaylistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.SelectableSongQuery
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.base.BaseViewModel
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import java.util.concurrent.TimeUnit


class AddSongToPlaylistViewModel constructor(
        private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
        private val schedulerProvider: SchedulerProvider,
        private val eventLogger: EventLogger
): BaseViewModel(eventLogger) {

    private val _typedQuery: MutableLiveData<String> = MutableLiveData()

    private val _selectableSongQuery: MutableLiveData<SelectableSongQuery> = MutableLiveData()
    val selectableSongQuery: LiveData<SelectableSongQuery> = _selectableSongQuery

    private val _selectedItems: MutableLiveData<Set<Song>> = MutableLiveData()
    val selectedItems: LiveData<Set<Song>> = _selectedItems

    val placeholderVisible: LiveData<Boolean> by lazy {
        Transformations.map(_selectableSongQuery) { query ->
            query.allItems.isNullOrEmpty()
        }
    }

    private val _isAddingSongsToPlaylist: MutableLiveData<Boolean> = MutableLiveData()
    val isAddingSongsToPlaylist: LiveData<Boolean> = _isAddingSongsToPlaylist

    private val _songsAddedToPlaylistEvent: MutableLiveData<Unit> = MutableLiveData()
    val songsAddedToPlaylistEvent: LiveData<Unit> = _songsAddedToPlaylistEvent

    private val queryPublisher: PublishProcessor<String> by lazy {
        val publisher = PublishProcessor.create<String>()
        publisher.debounce(300, TimeUnit.MILLISECONDS)
                .switchMap { query -> addSongToPlaylistUseCase.search(query) }
                .map { songs ->
                    SelectableSongQuery(songs, selectedItems.value ?: emptySet())
                }
                .observeOn(schedulerProvider.main())
                .subscribeFor { query ->
                    _selectableSongQuery.value = query
                }
        return@lazy publisher
    }

    fun onQuerySubmitted(query: String) {
        _typedQuery.value = query
        queryPublisher.onNext(query)
    }

    fun onQueryTyped(query: String) {
        _typedQuery.value = query
        queryPublisher.onNext(query)
    }

    fun onItemClicked(song: Song) {
        val operator = Single.fromCallable {
            val selectedItems = _selectedItems.value ?: emptySet()
            if (selectedItems.contains(song)) {
                selectedItems - song
            } else {
                selectedItems + song
            }
        }
        operator
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.main())
                .subscribeFor { selectedItems -> _selectedItems.value = selectedItems }
    }

    fun onItemLongClicked(song: Song) {
        onItemClicked(song)
    }

    fun onAddButtonClicked() {
        val songs = selectedItems.value ?: emptySet()
        addSongToPlaylistUseCase.addSongs(songs)
                .observeOn(schedulerProvider.main())
                .doOnSubscribe { _isAddingSongsToPlaylist.value = true }
                .doFinally { _isAddingSongsToPlaylist.value = false }
                .subscribeFor {
                    _songsAddedToPlaylistEvent.value = Unit
                }
    }

    fun onCloseSearchViewButtonClicked() {
        addSongToPlaylistUseCase.goBack()
    }
}