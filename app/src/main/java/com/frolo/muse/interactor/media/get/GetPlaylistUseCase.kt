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
    preferences
) {

    fun getPlaylist(): Flowable<Playlist> {
        return Flowable.just(playlist)
            .concatWith(playlistRepository.getItem(playlist))
            .subscribeOn(schedulerProvider.worker())
    }

    fun edit(freshVersion: Playlist? = null) {
        navigator.editPlaylist(freshVersion ?: playlist)
    }

    fun addSongs() {
        navigator.addSongsToPlaylist(playlist)
    }

    fun isCurrentSortOrderSwappable(): Flowable<Boolean> {
        return preferences.getSortOrderForSection(Library.PLAYLIST)
            .flatMapSingle { sortOrder ->
                playlistChunkRepository.isMovingAllowedForSortOrder(sortOrder)
            }
            .subscribeOn(schedulerProvider.worker())
    }

    override fun getSortedCollection(sortOrder: String): Flowable<List<Song>> {
        return playlistChunkRepository.getSongsFromPlaylist(playlist, sortOrder)
    }

    override fun removeDuplicatesIfNecessary(list: List<Song>): List<Song> {
        return if (playlist.isFromSharedStorage) {
            list.distinctBy { item -> item.id }
        } else {
            // No need to remove duplicates for playlists from the application storage
            list
        }
    }

    fun removeItem(song: Song): Completable {
        return playlistChunkRepository.removeFromPlaylist(playlist, song)
            .subscribeOn(schedulerProvider.worker())
    }

    fun moveItem(fromPosition: Int, toPosition: Int, snapshot: List<Song>): Completable {
        return preferences.getSortOrderForSection(Library.PLAYLIST)
            .firstOrError()
            .flatMap { sortOrder ->
                playlistChunkRepository.isMovingAllowedForSortOrder(sortOrder)
            }
            .flatMapCompletable { isMovingAllowed ->
                if (!isMovingAllowed) {
                    // The move op is not allowed => just complete
                    return@flatMapCompletable Completable.complete()
                }

                preferences
                    .isSortOrderReversedForSection(Library.PLAYLIST)
                    .firstOrError()
                    .flatMapCompletable { isReversed ->
                        if (playlist.isFromSharedStorage) {
                            moveItemLegacyImpl(fromPosition, toPosition, snapshot, isReversed)
                        } else {
                            moveItemImpl(fromPosition, toPosition, snapshot, isReversed)
                        }
                    }
            }
    }

    private fun moveItemLegacyImpl(
        fromPosition: Int,
        toPosition: Int,
        snapshot: List<Song>,
        isReversed: Boolean
    ): Completable {
        val listSize = snapshot.size
        val actualFromPosition: Int
        val actualToPosition: Int
        if (isReversed) {
            actualFromPosition = (listSize - 1) - fromPosition
            actualToPosition = (listSize - 1) - toPosition
        } else {
            actualFromPosition = fromPosition
            actualToPosition = toPosition
        }
        val source = playlistChunkRepository.moveItemInPlaylist(playlist, actualFromPosition, actualToPosition)
        return source.subscribeOn(schedulerProvider.worker())
    }

    private fun moveItemImpl(fromPosition: Int, toPosition: Int, snapshot: List<Song>, isReversed: Boolean): Completable {
        val previous: Song?
        val next: Song?
        when {
            fromPosition < toPosition -> {
                // The fromPosition goes before the toPosition
                if (isReversed) {
                    /**
                     (5) -----pos------
                     (4) -fromPosition-
                     (3) -----pos------
                     (2) -----pos------
                     (1) --toPosition--
                     (0) -----pos------
                     */
                    previous = snapshot.getOrNull(toPosition + 1)
                    next = snapshot.getOrNull(toPosition)
                } else {
                    /**
                     (1) -----pos------
                     (2) -fromPosition-
                     (3) -----pos------
                     (4) -----pos------
                     (5) --toPosition--
                     (6) -----pos------
                     */
                    previous = snapshot.getOrNull(toPosition)
                    next = snapshot.getOrNull(toPosition + 1)
                }
            }

            // The fromPosition goes after the toPosition
            fromPosition > toPosition -> {
                if (isReversed) {
                    /**
                    (5) -----pos------
                    (4) --toPosition--
                    (3) -----pos------
                    (2) -----pos------
                    (1) -fromPosition-
                    (0) -----pos------
                     */
                    previous = snapshot.getOrNull(toPosition)
                    next = snapshot.getOrNull(toPosition - 1)
                } else {
                    /**
                    (0) -----pos------
                    (1) --toPosition--
                    (2) -----pos------
                    (3) -----pos------
                    (4) -fromPosition-
                    (5) -----pos------
                     */
                    previous = snapshot.getOrNull(toPosition - 1)
                    next = snapshot.getOrNull(toPosition)
                }
            }

            // The fromPosition is the toPosition, which is an error
            else -> {
                // Should not happen
                val error = IllegalArgumentException("Position 'from' " +
                        "is equal to position 'to': $fromPosition")
                return Completable.error(error)
            }
        }
        val target: Song = snapshot[fromPosition]
        val moveOp = PlaylistChunkRepository.MoveOp(target, previous, next);
        return playlistChunkRepository.moveItemInPlaylist(moveOp)
    }

    @AssistedInject.Factory
    interface Factory {
        fun create(playlist: Playlist): GetPlaylistUseCase
    }

}