package com.frolo.muse.ui.main.library.playlists.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.engine.Player
import com.frolo.muse.navigator.Navigator
import com.frolo.muse.interactor.media.*
import com.frolo.muse.interactor.media.favourite.ChangeFavouriteUseCase
import com.frolo.muse.interactor.media.favourite.GetIsFavouriteUseCase
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
        getIsFavouriteUseCase: GetIsFavouriteUseCase<Song>,
        changeFavouriteUseCase: ChangeFavouriteUseCase<Song>,
        private val schedulerProvider: SchedulerProvider,
        private val navigator: Navigator,
        eventLogger: EventLogger
): AbsSongCollectionViewModel<Song>(
        player,
        getPlaylistUseCase,
        getMediaMenuUseCase,
        clickMediaUseCase,
        playMediaUseCase,
        shareMediaUseCase,
        deleteMediaUseCase,
        getIsFavouriteUseCase,
        changeFavouriteUseCase,
        schedulerProvider,
        navigator,
        eventLogger
) {

    private val _playlist: MutableLiveData<Playlist> by lazy {
        MutableLiveData<Playlist>().apply {
            getPlaylistUseCase.getPlaylist()
                .observeOn(schedulerProvider.main())
                .subscribeFor { value = it }
        }
    }
    val playlist: LiveData<Playlist> get() = _playlist

    private val _isSwappingEnabled: MutableLiveData<Boolean> by lazy {
        MediatorLiveData<Boolean>().apply {
            value = false

            getPlaylistUseCase
                .isCurrentSortOrderSwappable()
                .observeOn(schedulerProvider.main())
                .subscribeFor {
                    value = it
                }
        }
    }
    val isSwappingEnabled: LiveData<Boolean> get() = _isSwappingEnabled

    fun onEditPlaylistOptionSelected() {
        getPlaylistUseCase.edit()
    }

    fun onAddSongButtonClicked() {
        getPlaylistUseCase.addSongs()
    }

    fun onItemRemoved(item: Song) {
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

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        val listSize = mediaItemCount.value ?: return

        getPlaylistUseCase.moveItem(listSize, fromPosition, toPosition)
            .observeOn(schedulerProvider.main())
            .subscribeFor {
            }
    }

    fun onDragEnded() {
        submitMediaList(mediaList.value ?: emptyList())
    }
}