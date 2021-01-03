package com.frolo.muse.common

import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.model.media.*

fun AudioSourceQueue?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty
}

fun AudioSourceQueue.indexOf(predicate: (item: AudioSource) -> Boolean): Int {
    for (i in 0 until length) {
        if (predicate(getItemAt(i))) {
            return i
        }
    }
    return -1
}

fun AudioSourceQueue.findFirstOrNull(predicate: (item: AudioSource) -> Boolean): AudioSource? {
    for (i in 0 until length) {
        val item = getItemAt(i)
        if (predicate(item)) {
            return item
        }
    }
    return null
}

@Throws(NoSuchElementException::class)
fun AudioSourceQueue.find(predicate: (item: AudioSource) -> Boolean): AudioSource {
    return findFirstOrNull(predicate) ?: throw NoSuchElementException()
}

@Suppress("FunctionName")
fun AudioSourceQueue(songs: List<Song>, associatedMediaItem: Media?): AudioSourceQueue {
    val audioSources = songs.toAudioSources()
    return when(associatedMediaItem) {
        is Album -> AudioSourceQueue.create(AudioSourceQueue.ALBUM, associatedMediaItem.id, associatedMediaItem.name, audioSources)
        is Artist -> AudioSourceQueue.create(AudioSourceQueue.ARTIST, associatedMediaItem.id, associatedMediaItem.name, audioSources)
        is Genre -> AudioSourceQueue.create(AudioSourceQueue.GENRE, associatedMediaItem.id, associatedMediaItem.name, audioSources)
        is Playlist -> AudioSourceQueue.create(AudioSourceQueue.PLAYLIST, associatedMediaItem.id, associatedMediaItem.name, audioSources)
        else -> AudioSourceQueue.create(AudioSourceQueue.CHUNK, AudioSourceQueue.NO_ID, "", audioSources)
    }
}