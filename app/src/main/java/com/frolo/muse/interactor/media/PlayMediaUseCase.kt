package com.frolo.muse.interactor.media

import com.frolo.muse.common.AudioSourceQueue
import com.frolo.muse.engine.Player
import com.frolo.muse.common.prepareByTarget
import com.frolo.muse.common.toAudioSources
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable


class PlayMediaUseCase<E: Media> constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>,
        private val player: Player
) {

    private fun processPlay(songs: List<Song>, associatedMediaItem: Media?) {
        val songQueue = AudioSourceQueue(songs, associatedMediaItem)
        val first: AudioSource? = songQueue.let { queue ->
            if (queue.isEmpty) null
            else queue.getItemAt(0)
        }
        if (first != null) {
            player.prepareByTarget(songQueue, first, true)
        }
    }

    fun play(item: E, associatedMediaItem: Media? = null): Completable {
        return play(listOf(item), associatedMediaItem)
    }

    fun play(items: Collection<E>, associatedMediaItem: Media? = null): Completable {
        return repository.collectSongs(items)
                .subscribeOn(schedulerProvider.worker())
                .doOnSuccess { songs -> processPlay(songs, associatedMediaItem) }
                .ignoreElement()
    }

    fun playNext(item: E): Completable {
        return playNext(listOf(item))
    }

    fun playNext(items: Collection<E>): Completable {
        return repository.collectSongs(items)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.computation())
                .map { songs -> songs.toAudioSources() }
                .doOnSuccess { audioSources -> player.addAllNext(audioSources) }
                .ignoreElement()
    }

    fun addToQueue(item: E): Completable {
        return addToQueue(listOf(item))
    }

    fun addToQueue(items: Collection<E>): Completable {
        return repository.collectSongs(items)
                .subscribeOn(schedulerProvider.worker())
                .observeOn(schedulerProvider.computation())
                .map { songs -> songs.toAudioSources() }
                .doOnSuccess { audioSources -> player.addAll(audioSources) }
                .ignoreElement()
    }

}