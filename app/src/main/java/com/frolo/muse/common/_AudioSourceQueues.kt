package com.frolo.muse.common

import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSourceQueue
import com.frolo.muse.model.media.*


val TAG_ASSOCIATED_MEDIA = Any()
@Deprecated("Use TAG_ASSOCIATED_MEDIA")
val TAG_QUEUE_NAME = Any()
@Deprecated("Use TAG_ASSOCIATED_MEDIA")
val TAG_QUEUE_ID = Any()

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

fun AudioSourceQueue.first(): AudioSource {
    return getItemAt(0)
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

inline fun <T> AudioSourceQueue.map(transform: (AudioSource) -> T): List<T> {
    return snapshot.map(transform)
}

@Suppress("FunctionName")
fun AudioSourceQueue(songs: List<Song>, associatedMediaItem: Media?): AudioSourceQueue {
    val audioSources = songs.toAudioSources()
    val queue = AudioSourceQueue.create(audioSources)
    if (associatedMediaItem != null) {
        queue.putTag(TAG_ASSOCIATED_MEDIA, associatedMediaItem)
    }
    return queue
}

@Suppress("FunctionName")
fun AudioSourceQueue(song: Song): AudioSourceQueue {
    return AudioSourceQueue(listOf(song), song)
}

val AudioSourceQueue.associatedMedia: Media?
    get() {
        return this.getTag(TAG_ASSOCIATED_MEDIA) as? Media
    }