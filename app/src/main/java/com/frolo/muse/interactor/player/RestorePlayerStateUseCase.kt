package com.frolo.muse.interactor.player

import com.frolo.muse.common.*
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.interactor.media.get.excludeShortAudioSources
import com.frolo.muse.repository.*
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import javax.inject.Inject


class RestorePlayerStateUseCase @Inject constructor(
    private val schedulerProvider: SchedulerProvider,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val genreRepository: GenreRepository,
    private val playlistRepository: PlaylistRepository,
    private val preferences: Preferences,
    private val libraryPreferences: LibraryPreferences
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
            .map { songs -> AudioSourceQueue(songs, null) }
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
            .map { songs -> AudioSourceQueue(songs, null) }
            .doOnNext { queue ->
                if (queue.isEmpty) {
                    throw NullPointerException("Queue is empty")
                }
            }

        val queueFallbackSource: Flowable<AudioSourceQueue> = when (val type = preferences.lastMediaCollectionType) {
            AudioSourceQueue.ALBUM -> albumRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { album ->
                    albumRepository.collectSongs(album).map { songs -> AudioSourceQueue(songs, album) }
                }

            AudioSourceQueue.ARTIST -> artistRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { artist ->
                    artistRepository.collectSongs(artist).map { songs -> AudioSourceQueue(songs, artist) }
                }

            AudioSourceQueue.GENRE -> genreRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { genre ->
                    genreRepository.collectSongs(genre).map { songs -> AudioSourceQueue(songs, genre) }
                }

            AudioSourceQueue.PLAYLIST -> playlistRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { playlist ->
                    playlistRepository.collectSongs(playlist).map { songs -> AudioSourceQueue(songs, playlist) }
                }

            AudioSourceQueue.FAVOURITES -> songRepository.allFavouriteItems.map { songs ->
                AudioSourceQueue(songs, null)
            }

            else -> songRepository.allItems.map { songs ->
                AudioSourceQueue(songs, null)
            }
        }

        return queueSource.firstOrError()
            .onErrorResumeNext(queueFallbackSource.firstOrError())
            .flatMap { queue ->
                libraryPreferences.getMinAudioDuration()
                    .first(0)
                    .map { queue.excludeShortAudioSources(it) }
            }
            .map { queue ->

                val targetItem = queue.findFirstOrNull { item -> item.id == preferences.lastSongId }
                        ?: queue.first()

                PlayerState(
                    queue = queue,
                    targetItem = targetItem,
                    startPlaying = false,
                    playbackPosition = preferences.lastPlaybackPosition
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