package com.frolo.muse.interactor.player

import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SongQueue
import com.frolo.muse.engine.SongQueueFactory
import com.frolo.muse.engine.find
import com.frolo.muse.model.media.Song
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
        private val songQueueFactory: SongQueueFactory
) {

    private data class PlayerState constructor(
            val queue: SongQueue,
            val targetSong: Song,
            val playbackPosition: Int,
            val startPlaying: Boolean = false
    )

    private fun getDefaultPlayerState(): Single<PlayerState> {
        return songRepository.allItems
                .firstOrError()
                .map { songs ->
                    songQueueFactory.create(
                            SongQueue.CHUNK,
                            SongQueue.NO_ID,
                            "",
                            songs
                    )
                }
                .map { queue ->
                    PlayerState(
                            queue,
                            queue.getItemAt(0),
                            0,
                            false
                    )
                }
    }

    private fun forceRestorePlayerState(player: Player): Completable {
        @SongQueue.QueueType val type = preferences.lastMediaCollectionType

        val songQueueSource: Flowable<SongQueue> = when (type) {
            SongQueue.ALBUM -> albumRepository.getItem(preferences.lastMediaCollectionId)
                    .flatMapSingle { album ->
                        albumRepository.collectSongs(album)
                                .map { songs ->
                                    songQueueFactory.create(type, album.id, album.name, songs)
                                }
                    }

            SongQueue.ARTIST -> artistRepository.getItem(preferences.lastMediaCollectionId)
                    .flatMapSingle { artist ->
                        artistRepository.collectSongs(artist)
                                .map { songs ->
                                    songQueueFactory.create(type, artist.id, artist.name, songs)
                                }
                    }

            SongQueue.GENRE -> genreRepository.getItem(preferences.lastMediaCollectionId)
                    .flatMapSingle { genre ->
                        genreRepository.collectSongs(genre)
                                .map { songs ->
                                    songQueueFactory.create(type, genre.id, genre.name, songs)
                                }
                    }

            SongQueue.PLAYLIST -> playlistRepository.getItem(preferences.lastMediaCollectionId)
                    .flatMapSingle { playlist ->
                        playlistRepository.collectSongs(playlist)
                                .map { songs ->
                                    songQueueFactory.create(type, playlist.id, playlist.name, songs)
                                }
                    }

            SongQueue.FAVOURITES -> songRepository.allFavouriteItems.map { songs ->
                songQueueFactory.create(type, SongQueue.NO_ID, "", songs)
            }

            else -> songRepository.allItems.map { songs ->
                songQueueFactory.create(type, SongQueue.NO_ID, "", songs)
            }
        }

        // check for result. if the returned queue is empty then fetch default one
        return songQueueSource
                .firstOrError()
                .map { songQueue ->

                    val targetSong = songQueue.find { item ->
                        item.id == preferences.lastSongId }

                    PlayerState(
                            songQueue,
                            targetSong,
                            preferences.lastPlaybackPosition)
                }
                .onErrorResumeNext(getDefaultPlayerState())
                .doOnSuccess { playerState ->
                    player.prepare(
                            playerState.queue,
                            playerState.targetSong,
                            playerState.playbackPosition,
                            playerState.startPlaying)
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