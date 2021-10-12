package com.frolo.muse

import com.frolo.muse.engine.AudioMetadata
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSources
import com.frolo.muse.engine.AudioType


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