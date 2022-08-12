package com.frolo.muse.interactor.media.get

import androidx.annotation.WorkerThread
import com.frolo.muse.common.audioType
import com.frolo.muse.common.durationInSeconds
import com.frolo.muse.common.toSongType
import com.frolo.player.AudioSource
import com.frolo.player.AudioSourceQueue
import com.frolo.player.Player
import com.frolo.music.model.SongType


@WorkerThread
fun <A: AudioSource> List<A>.excludeShortAudioSources(minDurationInSeconds: Long): List<A> {
    if (minDurationInSeconds <= 0) return this
    return filter { it.durationInSeconds >= minDurationInSeconds }
}

@WorkerThread
fun AudioSourceQueue.excludeShortAudioSources(minDurationInSeconds: Long): AudioSourceQueue {
    val filteredItems = snapshot.excludeShortAudioSources(minDurationInSeconds)
    return AudioSourceQueue.create(filteredItems)
}

@WorkerThread
fun Player.removeShortAudioSources(minDurationInSeconds: Long) {
    val currentAudioSources = getCurrentQueue()?.snapshot ?: return
    val itemsToRemove = currentAudioSources.filter { it.durationInSeconds < minDurationInSeconds }
    removeAll(itemsToRemove)
}

@WorkerThread
fun Player.retainItemsWithSongTypes(types: Collection<SongType>) {
    val queue = getCurrentQueue() ?: return
    val itemsToRemove = queue.snapshot.orEmpty().filter { item ->
        val songType = item.audioType.toSongType()
        songType !in types
    }
    removeAll(itemsToRemove)
}