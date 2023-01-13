package com.frolo.music.model.test

import com.frolo.music.model.Media
import com.frolo.music.model.Song
import com.frolo.music.model.SongType
import com.frolo.music.model.Songs
import com.frolo.test.*

private class TestMedia constructor(private val _id: Long): Media {
    override fun getId(): Long = _id
    override fun getKind(): Int = Media.NONE
}

fun stubMedia(): Media = TestMedia(randomLong())

fun stubSong(
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

fun stubMediaList(size: Int): List<Media> {
    return List(size) { index -> TestMedia(index.toLong()) }
}

fun stubSongList(size: Int): List<Song> {
    return List(size) { index -> stubSong(id = index.toLong()) }
}

