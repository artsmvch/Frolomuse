package com.frolo.muse.player

import com.frolo.muse.common.*
import com.frolo.player.Player
import com.frolo.player.AudioSourceQueue
import com.frolo.player.AudioSource
import com.frolo.muse.repository.*
import com.frolo.muse.rx.SchedulerProvider
import com.frolo.music.repository.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class PlayerStateRestorer @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val genreRepository: GenreRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferences: Preferences
) {

    private data class PlayerState constructor(
        val queue: AudioSourceQueue,
        val targetItem: AudioSource,
        val startPlaying: Boolean,
        val playbackPosition: Int
    )

    private fun getDefaultPlayerState(): Single<PlayerState> {
        return songRepository.allItems
            .firstOrError()
            .flatMap { songs -> createAudioSourceQueue(songs, null) }
            .map { queue ->
                PlayerState(
                    queue = queue,
                    targetItem = queue.first(),
                    startPlaying = false,
                    playbackPosition = 0
                )
            }
    }

    private fun forceRestorePlayerState(player: Player): Completable {
        val queueSource: Flowable<AudioSourceQueue> = preferences.lastMediaCollectionItemIds
            .switchMap { ids -> songRepository.getSongsOptionally(ids) }
            .switchMapSingle { songs -> createAudioSourceQueue(songs, null) }
            .doOnNext { queue ->
                if (queue.isEmpty) {
                    throw NullPointerException("Queue is empty")
                }
            }

        val queueFallbackSource: Flowable<AudioSourceQueue> = when (preferences.lastMediaCollectionType) {
            /* deprecated */ 1 -> albumRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { album ->
                    albumRepository.collectSongs(album).flatMap { songs -> createAudioSourceQueue(songs, album) }
                }

            /* deprecated */ 2 -> artistRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { artist ->
                    artistRepository.collectSongs(artist).flatMap { songs -> createAudioSourceQueue(songs, artist) }
                }

            /* deprecated */ 3 -> genreRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { genre ->
                    genreRepository.collectSongs(genre).flatMap { songs -> createAudioSourceQueue(songs, genre) }
                }

            /* deprecated */ 4 -> playlistRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { playlist ->
                    playlistRepository.collectSongs(playlist).flatMap { songs -> createAudioSourceQueue(songs, playlist) }
                }

            /* deprecated */ 7 -> songRepository.allFavouriteItems.flatMapSingle { songs ->
                createAudioSourceQueue(songs, null)
            }

            else -> songRepository.allItems.flatMapSingle { songs ->
                createAudioSourceQueue(songs, null)
            }
        }

        return queueSource.firstOrError()
            .onErrorResumeNext(queueFallbackSource.firstOrError())
            .map { queue ->

                val targetItemId = preferences.lastSongId
                val targetItem = queue.findFirstOrNull { item -> item.id == targetItemId }
                val playbackProgress = if (targetItem != null) preferences.lastPlaybackPosition else 0

                PlayerState(
                    queue = queue,
                    targetItem = targetItem ?: queue.first(),
                    startPlaying = false,
                    playbackPosition = playbackProgress
                )
            }
            .onErrorResumeNext(getDefaultPlayerState())
            .doOnSuccess { playerState ->
                val queue = player.getCurrentQueue()
                if (queue != null && !queue.isEmpty) {
                    // no need to set the new queue, the player already has a non-empty one
                    return@doOnSuccess
                }
                player.prepareByTarget(
                    playerState.queue,
                    playerState.targetItem,
                    playerState.startPlaying,
                    playerState.playbackPosition
                )
            }
            .ignoreElement()
            .subscribeOn(schedulerProvider.worker())
    }

    fun restorePlayerStateIfNeeded(player: Player): Completable {
        val currentQueue = player.getCurrentQueue()
        return if (currentQueue == null || currentQueue.isEmpty) {
            // Restore the state ONLY if the player has no attached queue
            forceRestorePlayerState(player)
        } else {
            Completable.complete()
        }
    }

}