package com.frolo.muse.interactor.media

import com.frolo.muse.common.toAudioSources
import com.frolo.muse.engine.Player
import com.frolo.muse.model.event.DeletionType
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.PlaylistChunkRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single


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
            repository.collectSongs(item)
                .flatMapCompletable { songsRelatedToItem ->
                    repository.delete(item)
                        .doOnComplete {
                            val audioSources = songsRelatedToItem.toAudioSources()
                            player.removeAll(audioSources)
                        }
                }
        }
        return completable.subscribeOn(schedulerProvider.worker())
    }

    fun delete(items: Collection<E>, type: DeletionType): Completable {
        return Single.fromCallable { items.map { delete(it, type) } }
            .subscribeOn(schedulerProvider.computation())
            .flatMapCompletable { Completable.concat(it) }
    }

}