package com.frolo.muse.engine

import com.frolo.muse.model.media.Song


fun SongQueue.indexOf(predicate: (item: Song) -> Boolean): Int {
    for (i in 0 until length) {
        if (predicate(getItemAt(i))) {
            return i
        }
    }
    return -1
}

fun SongQueue.findFirstOrNull(predicate: (item: Song) -> Boolean): Song? {
    for (i in 0 until length) {
        val item = getItemAt(i)
        if (predicate(item)) {
            return item
        }
    }
    return null
}

fun SongQueue.find(predicate: (item: Song) -> Boolean): Song {
    return findFirstOrNull(predicate)
            ?: throw NoSuchElementException()
}