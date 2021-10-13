package com.frolo.muse.interactor.media.get

import com.frolo.muse.common.durationInSeconds
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.engine.Player


fun <A: AudioSource> List<A>.excludeShortAudioSources(minDurationInSeconds: Long): List<A> {
    if (minDurationInSeconds <= 0) return this
    return filter { it.durationInSeconds >= minDurationInSeconds }
}

fun AudioSourceQueue.excludeShortAudioSources(minDurationInSeconds: Long): AudioSourceQueue {
    val filteredItems = snapshot.excludeShortAudioSources(minDurationInSeconds)
    return AudioSourceQueue.create(type, id, name, filteredItems)
}

fun Player.removeShortAudioSources(minDurationInSeconds: Long) {
    val currentAudioSources = getCurrentQueue()?.snapshot ?: return
    val itemsToRemove = currentAudioSources.filter { it.durationInSeconds < minDurationInSeconds }
    removeAll(itemsToRemove)
}