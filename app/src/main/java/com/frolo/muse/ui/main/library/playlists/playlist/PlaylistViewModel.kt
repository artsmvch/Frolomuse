package com.frolo.muse.ui.main.library.playlists.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.get.GetPlaylistUseCase
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.muse.ui.main.library.base.AbsSongCollectionViewModel
import io.reactivex.Single


class PlaylistViewModel constructor(
        player: Player,
        private val getPlaylistUseCase: GetPlaylistUseCase,
        getMediaMenuUseCase: GetMediaMenuUseCase<Song>,
        clickMediaUseCase: ClickMediaUseCase<Song>,
        playMediaUseCase: PlayMediaUseCase<Song>,
        shareMediaUseCase: ShareMediaUseCase<Song>,
        deleteMediaUseCase: DeleteMediaUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        private val schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        eventLogger: EventLogger
): AbsSongCollectionViewModel(
        player,
        getPlaylistUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        changeFavouriteUseCase,
        schedulerProvider,
        navigator,
        eventLogger
) {

    private val _playlist: MutableLiveData<Playlist> by lazy {
        MutableLiveData<Playlist>().apply {
            getPlaylistUseCase.getPlaylist()
                    .observeOn(schedulerProvider.main())
                    .subscribeFor { item ->
                        value = item
                    }
        }
    }
    val playlist: LiveData<Playlist> get() = _playlist

    private val _isSwappingEnabled: MutableLiveData<Boolean> by lazy {
        MediatorLiveData<Boolean>().apply {
            value = getPlaylistUseCase
                    .isCurrentSortOrderSwappable()
                    .blockingGet()
        }
    }
    val isSwappingEnabled: LiveData<Boolean> = _isSwappingEnabled

    override fun onSortOrderSelected(sortOrder: String) {
        super.onSortOrderSelected(sortOrder)
        _isSwappingEnabled.value = getPlaylistUseCase
                .isSortOrderSwappable(sortOrder)
                .blockingGet()
    }

    fun onEditPlaylistOptionSelected() {
        getPlaylistUseCase.edit()
    }

    fun onAddSongButtonClicked() {
        getPlaylistUseCase.addSongs()
    }

    fun onRemoveItem(item: Song) {
        getPlaylistUseCase.removeItem(item)
                .andThen(Single.fromCallable {
                    mediaList.value.let { list ->
                        if (list != null) {
                            list - item
                        } else emptyList()
                    }
                })
                .observeOn(schedulerProvider.main())
                .subscribeFor { newList ->
                    submitMediaList(newList)
                }
    }

    fun onSwapItems(fromPosition: Int, toPosition: Int) {
        val listSize = mediaItemCount.value ?: return

        getPlaylistUseCase.swapItems(listSize, fromPosition, toPosition)
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                }
    }

    fun onFinishInteracting() {
        submitMediaList(mediaList.value ?: emptyList())
    }
}