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
        return if (playlist.isFromSharedStorage) {
            moveItemLegacyImpl(fromPosition, toPosition, snapshot.size)
        } else {
            moveItemImpl(fromPosition, toPosition, snapshot)
        }
    }

    private fun moveItemLegacyImpl(fromPosition: Int, toPosition: Int, listSize: Int): Completable {
        return preferences.getSortOrderForSection(Library.PLAYLIST)
            .firstOrError()
            .flatMap { sortOrder ->
                playlistChunkRepository.isMovingAllowedForSortOrder(sortOrder)
            }
            .flatMapCompletable { isMovingAllowed ->
                if (isMovingAllowed) {
                    val isReversed = preferences
                        .isSortOrderReversedForSection(Library.PLAYLIST)
                        .blockingFirst()

                    val actualFromPosition = if (isReversed) {
                        (listSize - 1) - fromPosition
                    } else fromPosition

                    val actualToPosition = if (isReversed) {
                        (listSize - 1) - toPosition
                    } else toPosition

                    playlistChunkRepository.moveItemInPlaylist(
                        playlist,
                        actualFromPosition,
                        actualToPosition
                    )

                } else Completable.complete()
            }
            .subscribeOn(schedulerProvider.worker())
    }

    private fun moveItemImpl(fromPosition: Int, toPosition: Int, snapshot: List<Song>): Completable {
        val previous: Song?
        val next: Song?
        when {
            fromPosition < toPosition -> {
                previous = snapshot.getOrNull(toPosition)
                next = snapshot.getOrNull(toPosition + 1)
            }
            fromPosition > toPosition -> {
                previous = snapshot.getOrNull(toPosition - 1)
                next = snapshot.getOrNull(toPosition)
            }
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