package com.frolo.muse.interactor.media.get

import com.frolo.muse.navigator.Navigator
import com.frolo.muse.model.Library
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.PlaylistChunkRepository
import com.frolo.muse.repository.PlaylistRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single


class GetPlaylistUseCase @AssistedInject constructor(
        private val schedulerProvider: SchedulerProvider,
        private val playlistRepository: PlaylistRepository,
        private val playlistChunkRepository: PlaylistChunkRepository,
        private val preferences: Preferences,
        private val navigator: Navigator,
        @Assisted private val playlist: Playlist
): GetSectionedMediaUseCase<Song>(
        Library.PLAYLIST,
        schedulerProvider,
        playlistChunkRepository,
        preferences) {

    fun getPlaylist(): Flowable<Playlist> {
        return Flowable.just(playlist)
                .concatWith(playlistRepository.getItem(playlist.id))
    }

    fun edit() {
        navigator.editPlaylist(playlist)
    }

    fun addSongs() {
        navigator.addSongsToPlaylist(playlist)
    }

    fun isSortOrderSwappable(sortOrder: String): Single<Boolean> {
        return playlistChunkRepository.isSwappingAllowedForSortOrder(sortOrder)
    }

    fun isCurrentSortOrderSwappable(): Single<Boolean> {
        return preferences.getSortOrderForSection(Library.PLAYLIST).let { sortOrder ->
            playlistChunkRepository.isSwappingAllowedForSortOrder(sortOrder)
        }
    }

    override fun getSortedCollection(sortOrder: String): Flowable<List<Song>> {
        return playlistChunkRepository.getSongsFromPlaylist(playlist, sortOrder)
    }

    fun removeItem(song: Song): Completable {
        return playlistChunkRepository.removeFromPlaylist(playlist, song)
                .subscribeOn(schedulerProvider.worker())
    }

    fun swapItems(listSize: Int, fromPosition: Int, toPosition: Int): Completable {
        return playlistChunkRepository.isSwappingAllowedForSortOrder(
                preferences.getSortOrderForSection(Library.PLAYLIST))
                .flatMapCompletable { isSwappingAllowed ->
                    if (isSwappingAllowed) {
                        val isReversed = preferences
                                .isSortOrderReversedForSection(Library.PLAYLIST)

                        val actualFromPosition = if (isReversed) {
                            (listSize - 1) - fromPosition
                        } else fromPosition

                        val actualToPosition = if (isReversed) {
                            (listSize - 1) - toPosition
                        } else toPosition

                        playlistChunkRepository.swapItemsInPlaylist(
                                playlist,
                                actualFromPosition,
                                actualToPosition)
                    } else Completable.complete()
                }
                .subscribeOn(schedulerProvider.worker())
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(playlist: Playlist): GetPlaylistUseCase
    }

}