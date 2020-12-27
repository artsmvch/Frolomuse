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
        private val audioSourceQueueFactory: AudioSourceQueueFactory
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
                .map { songs ->
                    audioSourceQueueFactory.create(AudioSourceQueue.CHUNK, AudioSourceQueue.NO_ID, "", songs)
                }
                .map { queue ->
                    val first = queue.getItemAt(0)
                    PlayerState(
                        queue = queue,
                        targetItem = first,
                        startPlaying = false,
                        playbackPosition = 0
                    )
                }
    }

    private fun forceRestorePlayerState(player: Player): Completable {
        @AudioSourceQueue.QueueType val type = preferences.lastMediaCollectionType

        val songQueueSource: Flowable<AudioSourceQueue> = when (type) {
            AudioSourceQueue.ALBUM -> albumRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { album ->
                    albumRepository.collectSongs(album)
                        .map { songs ->
                            audioSourceQueueFactory.create(type, album.id, album.name, songs)
                        }
                }

            AudioSourceQueue.ARTIST -> artistRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { artist ->
                    artistRepository.collectSongs(artist)
                        .map { songs ->
                            audioSourceQueueFactory.create(type, artist.id, artist.name, songs)
                        }
                }

            AudioSourceQueue.GENRE -> genreRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { genre ->
                    genreRepository.collectSongs(genre)
                        .map { songs ->
                            audioSourceQueueFactory.create(type, genre.id, genre.name, songs)
                        }
                }

            AudioSourceQueue.PLAYLIST -> playlistRepository.getItem(preferences.lastMediaCollectionId)
                .flatMapSingle { playlist ->
                    playlistRepository.collectSongs(playlist)
                        .map { songs ->
                            audioSourceQueueFactory.create(type, playlist.id, playlist.name, songs)
                        }
                }

            AudioSourceQueue.FAVOURITES -> songRepository.allFavouriteItems.map { songs ->
                audioSourceQueueFactory.create(type, AudioSourceQueue.NO_ID, "", songs)
            }

            else -> songRepository.allItems.map { songs ->
                audioSourceQueueFactory.create(type, AudioSourceQueue.NO_ID, "", songs)
            }
        }

        // check for result. if the returned queue is empty then fetch default one
        return songQueueSource
                .firstOrError()
                .flatMap { queue ->
                    preferences.minAudioFileDuration
                        .first(0)
                        .map { queue.excludeShortAudioSources(it) }
                }
                .map { queue ->

                    val targetItem = queue.findFirstOrNull { item ->
                        item.id == preferences.lastSongId
                    } ?: queue.getItemAt(0)

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