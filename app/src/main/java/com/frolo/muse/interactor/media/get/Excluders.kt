package com.frolo.muse.interactor.media.get

import com.frolo.muse.common.durationInSeconds
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.engine.Player
import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song
import com.frolo.muse.repository.Preferences
import io.reactivex.Flowable


fun <A: AudioSource> List<A>.excludeShortAudioSources(minDurationInSeconds: Int): List<A> {
    if (minDurationInSeconds <= 0) return this
    return filter { it.durationInSeconds >= minDurationInSeconds }
}

fun <S: Song> List<S>.excludeShortSongs(minDurationInSeconds: Int): List<S> {
    if (minDurationInSeconds <= 0) return this
    return filter { it.durationInSeconds >= minDurationInSeconds }
}

fun <M: Media> List<M>.excludeShortAudioFiles(minDurationInSeconds: Int): List<M> {
    if (minDurationInSeconds <= 0) return this
    return filter { it !is Song || it.durationInSeconds >= minDurationInSeconds }
}

fun AudioSourceQueue.excludeShortAudioSources(minDurationInSeconds: Int): AudioSourceQueue {
    val filteredItems = snapshot.excludeShortAudioSources(minDurationInSeconds)
    return AudioSourceQueue.create(type, id, name, filteredItems)
}

fun Player.removeShortAudioSources(minDurationInSeconds: Int) {
    val currentAudioSources = getCurrentQueue()?.snapshot ?: return
    val itemsToRemove = currentAudioSources.filter { it.durationInSeconds < minDurationInSeconds }
    removeAll(itemsToRemove)
}

/**
 * Excludes short songs from lists emitted by [this] Flowable source.
 * The min duration is defined by [Preferences.getMinAudioFileDuration].
 */
fun <T: Song> Flowable<List<T>>.excludeShortSongs(preferences: Preferences): Flowable<List<T>> {
    val sources = listOf(this, preferences.minAudioFileDuration)
    return Flowable.combineLatest(sources) { arr ->
        val songs = arr[0] as List<T>
        val minDuration = arr[1] as Int
        songs.excludeShortSongs(minDuration)
    }
}

/**
 * Excludes short songs from lists emitted by [this] Flowable source.
 * The min duration is defined by [Preferences.getMinAudioFileDuration].
 */
fun <T: Media> Flowable<List<T>>.excludeShortAudioFiles(preferences: Preferences): Flowable<List<T>> {
    val sources = listOf(this, preferences.minAudioFileDuration)
    return Flowable.combineLatest(sources) { arr ->
        val mediaList = arr[0] as List<T>
        val minDuration = arr[1] as Int
        mediaList.excludeShortAudioFiles(minDuration)
    }
}