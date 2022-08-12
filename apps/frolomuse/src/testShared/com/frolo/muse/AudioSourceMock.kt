package com.frolo.muse

import com.frolo.player.AudioMetadata
import com.frolo.player.AudioSource
import com.frolo.player.data.AudioSources
import com.frolo.player.AudioType
import com.frolo.test.*


fun mockAudioSource(
    id: Long = randomLong(),
    source: String = randomString(),
    audioType: AudioType = randomEnumValue<AudioType>()!!,
    title: String = randomString(),
    albumId: Long = randomLong(),
    album: String = randomString(),
    artistId: Long = randomLong(),
    artist: String = randomString(),
    genre: String = randomString(),
    duration: Int = randomInt(),
    year: Int = randomInt(),
    trackNumber: Int = randomInt()
): AudioSource {
    val metadata = AudioSources.createMetadata(
        audioType, title, albumId, album, artistId, artist, genre, duration, year, trackNumber)
    return AudioSources.createAudioSource(id, source, metadata)
}

fun mockAudioMetadata(
    audioType: AudioType = randomEnumValue<AudioType>()!!,
    title: String = randomString(),
    albumId: Long = randomLong(),
    album: String = randomString(),
    artistId: Long = randomLong(),
    artist: String = randomString(),
    genre: String = randomString(),
    duration: Int = randomInt(),
    year: Int = randomInt(),
    trackNumber: Int = randomInt()
): AudioMetadata = AudioSources.createMetadata(audioType, title, albumId, album, artistId, artist, genre, duration, year, trackNumber)