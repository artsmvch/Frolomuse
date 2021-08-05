package java.com.frolo.muse

import com.frolo.muse.engine.AudioMetadata
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSources


fun mockAudioSource(
    id: Long = randomLong(),
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
): AudioSource {
    val metadata = AudioSources.createMetadata(
            title, albumId, album, artistId, artist, genre, duration, year, trackNumber)
    return AudioSources.createAudioSource(id, source, metadata)
}

fun mockAudioMetadata(
    title: String = randomString(),
    albumId: Long = randomLong(),
    album: String = randomString(),
    artistId: Long = randomLong(),
    artist: String = randomString(),
    genre: String = randomString(),
    duration: Int = randomInt(),
    year: Int = randomInt(),
    trackNumber: Int = randomInt()
): AudioMetadata = AudioSources.createMetadata(title, albumId, album, artistId, artist, genre, duration, year, trackNumber)