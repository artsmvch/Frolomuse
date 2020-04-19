package com.frolo.muse

import com.frolo.muse.model.media.Media
import com.frolo.muse.model.media.Song
import com.nhaarman.mockitokotlin2.mock


fun mockMedia(): Media {
    return TestMedia(0.toLong())
}

fun mockMediaList(size: Int = 1, allowIdCollisions: Boolean = false): List<Media> {
    val list = ArrayList<Media>(size)
    repeat(size) { index ->
        val item: Media = if (allowIdCollisions) {
            mock()
        } else {
            TestMedia(index.toLong())
        }
        list.add(item)
    }
    return list
}

fun mockSongList(size: Int = 1, allowIdCollisions: Boolean = false): List<Song> {
    val list = ArrayList<Song>(size)
    repeat(size) { index ->
        val item: Song = if (allowIdCollisions) {
            mock()
        } else {
            SongImpl(
                    index.toLong(),
                    mockKT(),
                    mockKT(),
                    mockKT(),
                    mockKT(),
                    mockKT(),
                    mockKT(),
                    mockKT(),
                    mockKT(),
                    mockKT(),
                    mockKT()
            )
        }
        list.add(item)
    }
    return list
}

