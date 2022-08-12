package com.frolo.muse.interactor.media

import com.frolo.muse.common.createAudioSourceQueue
import com.frolo.player.Player
import com.frolo.player.prepareByTarget
import com.frolo.muse.common.toAudioSources
import com.frolo.player.AudioSource
import com.frolo.music.model.Media
import com.frolo.music.repository.MediaRepository
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable


class PlayMediaUseCase<E: Media> constructor(
    private val schedulerProvider: SchedulerProvider,
    private val repository: MediaRepository<E>,
    private val player: Player
) {

    fun play(item: E, associatedMediaItem: Media? = null): Completable {
        return play(listOf(item), associatedMediaItem)
    }

    fun play(items: Collection<E>, associatedMediaItem: Media? = null): Completable {
        return repository.collectSongs(items)
            .flatMap { songs -> createAudioSourceQueue(songs, associatedMediaItem) }
            .doOnSuccess { queue ->
                val first: AudioSource? = if (!queue.isEmpty) queue.getItemAt(0) else null
                if (first != null) {
                    player.prepareByTarget(queue, first, true)
                }
            }
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