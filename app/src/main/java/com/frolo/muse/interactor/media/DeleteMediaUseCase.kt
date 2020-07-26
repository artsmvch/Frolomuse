package com.frolo.muse.interactor.media

import com.frolo.muse.common.toAudioSources
import com.frolo.muse.engine.Player
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Playlist
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Function


class DeleteMediaUseCase <E: Media> constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>,
        private val player: Player
) {

    fun delete(item: E): Completable {
        val operator = if (item is Playlist) {
            repository.delete(item)
        } else {
            repository.collectSongs(item)
                    .flatMapCompletable { songsRelatedToItem ->
                        repository.delete(item)
                                .doOnComplete {
                                    val audioSources = songsRelatedToItem.toAudioSources()
                                    player.removeAll(audioSources)
                                }
                    }
        }
        return operator
                .subscribeOn(schedulerProvider.worker())
    }

    fun delete(items: Collection<E>): Completable {
        val removeFromPlaylist = items.let { list ->
            val sources = list.map { item ->
                repository.collectSongs(item)
            }

            Single.zip(sources, Function<Array<Any>, List<Song>> { arr ->
                arr.flatMap {
                    @Suppress("UNCHECKED_CAST")
                    it as Collection<Song>
                }
            })
        }

        return removeFromPlaylist
                .doOnSuccess { songs -> player.removeAll(songs.toAudioSources()) }
                .flatMapCompletable { repository.delete(items) }
                .subscribeOn(schedulerProvider.worker())
    }

}