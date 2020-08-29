package com.frolo.muse.interactor.media

import com.frolo.muse.engine.Player
import com.frolo.muse.common.AudioSourceQueueFactory
import com.frolo.muse.common.prepareByTarget
import com.frolo.muse.common.toAudioSources
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.MediaRepository
import com.frolo.muse.repository.Preferences
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable


class PlayMediaUseCase<E: Media> constructor(
        private val schedulerProvider: SchedulerProvider,
        private val repository: MediaRepository<E>,
        private val preferences: Preferences,
        private val player: Player,
        private val audioSourceQueueFactory: AudioSourceQueueFactory
) {

    private fun processPlay(targets: Collection<E>, songs: List<Song>) {
        if (targets.size == 1) {
            targets.firstOrNull()?.let { safeTarget ->
                preferences.saveLastMediaCollectionType(safeTarget.kind)
                preferences.saveLastMediaCollectionId(safeTarget.id)
            }
        }

        val songQueue = audioSourceQueueFactory.create(targets.toList(), songs)
        val first: AudioSource? = songQueue.let { queue ->
            if (queue.isEmpty) null
            else queue.getItemAt(0)
        }
        if (first != null) {
            preferences.saveLastSongId(first.id)
            player.prepareByTarget(songQueue, first, true)
        }
    }

    fun play(item: E): Completable {
        return play(listOf(item))
    }

    fun play(items: Collection<E>): Completable {
        return repository.collectSongs(items)
                .subscribeOn(schedulerProvider.worker())
                .doOnSuccess { songs -> processPlay(items, songs) }
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