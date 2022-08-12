package com.frolo.muse.interactor.media

import com.frolo.muse.common.toAudioSources
import com.frolo.player.Player
import com.frolo.muse.kotlin.containsInstanceOf
import com.frolo.muse.model.event.DeletionType
import com.frolo.music.model.Media
import com.frolo.music.model.Playlist
import com.frolo.music.model.Song
import com.frolo.music.repository.MediaRepository
import com.frolo.music.repository.PlaylistChunkRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable


class DeleteMediaUseCase <E: Media> constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: MediaRepository<E>,
    private val playlistChunkRepository: PlaylistChunkRepository,
    private val player: Player
) {

    fun delete(item: E, type: DeletionType): Completable {
        val completable = if (item is Playlist) {
            // If the item is a Playlist then we simply delete it.
            repository.delete(item)
        } else if (item is Song && type is DeletionType.FromAssociatedMedia && type.media is Playlist) {
            // The user only wants to remove the song from the playlist
            playlistChunkRepository.removeFromPlaylist(type.media, item)
        } else {
            // Otherwise, we collect all related to this item songs and remove those songs from the player.
            // After that, we delete the item.
            deleteMediaItemAndRemoveItFromPlayerQueue(item)
        }
        return completable.subscribeOn(schedulerProvider.worker())
    }

    fun delete(items: Collection<E>, type: DeletionType): Completable {
        return Completable.defer {
            val completable = if (type is DeletionType.FromAssociatedMedia && type.media is Playlist) {
                val op1 = kotlin.run {
                    // Batch remove op
                    val songs = items.filterIsInstance<Song>()
                    playlistChunkRepository.removeFromPlaylist(type.media, songs)
                }
                val op2 = kotlin.run {
                    // Batch delete op
                    val nonSongs = items.filterNot { it is Song }
                    deleteMediaItemsAndRemoveThemFromPlayerQueue(nonSongs)
                }
                Completable.concat(listOf(op1, op2))
            } else if (items.containsInstanceOf<Playlist>()) {
                // There are playlists in the collection, just delete them without removing them from the player queue
                repository.delete(items)
            } else {
                deleteMediaItemsAndRemoveThemFromPlayerQueue(items)
            }
            completable.subscribeOn(schedulerProvider.worker())
        }.subscribeOn(schedulerProvider.computation())
    }

    private fun deleteMediaItemAndRemoveItFromPlayerQueue(item: E): Completable {
        return deleteMediaItemsAndRemoveThemFromPlayerQueue(listOf(item))
    }

    private fun deleteMediaItemsAndRemoveThemFromPlayerQueue(items: Collection<E>): Completable {
        return repository.collectSongs(items)
            .flatMapCompletable { songsRelatedToItems ->
                repository.delete(items)
                    .doOnComplete {
                        val audioSources = songsRelatedToItems.toAudioSources()
                        player.removeAll(audioSources)
                    }
            }
    }

}