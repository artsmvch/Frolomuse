package com.frolo.muse

import com.frolo.music.model.Media
import com.frolo.music.model.Song
import com.frolo.music.model.SongType
import com.frolo.music.model.Songs
import com.frolo.test.*
import com.nhaarman.mockitokotlin2.mock


fun mockMedia(): Media = TestMedia(0.toLong())

fun mockSong(
    id: Long = randomLong(),
    songType: SongType = randomEnumValue<SongType>()!!,
    source: String = randomString(),
    title: String = randomString(),
    albumId: Long = randomLong(),
    album: String = randomString(),
    artistId: Long = randomLong(),
    artist: String = randomString(),
    genre: String = randomString(),
    duration: Int = randomInt(),
    year: Int = randomInt(),
    trackNumber: Int = randomInt()
): Song = Songs.create(id, songType, source, title, albumId, album, artistId, artist, genre, duration, year, trackNumber)

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
            mockSong()
        } else {
            mockSong(id = index.toLong())
        }
        list.add(item)
    }
    return list
}

