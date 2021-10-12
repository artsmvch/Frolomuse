package com.frolo.muse.common

import com.frolo.muse.engine.AudioMetadata
import com.frolo.muse.engine.AudioSource
import com.frolo.muse.engine.AudioSources
import com.frolo.muse.engine.AudioType
import com.frolo.muse.mockAudioSource
import com.frolo.muse.mockSong
import com.frolo.muse.model.media.Song
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.random.Random


@RunWith(JUnit4::class)
class AudioSourcesTest {

    private val random = Random(System.currentTimeMillis())

    private val assertAudioSourcesEqual: (AudioSource, AudioSource) -> Unit = { audioSource1, audioSource2 ->
        assertEquals(audioSource1.id, audioSource2.id)
        assertEquals(audioSource1.source, audioSource2.source)

        assertEquals(audioSource1.title, audioSource2.title)
        assertEquals(audioSource1.artistId, audioSource2.artistId)
        assertEquals(audioSource1.artist, audioSource2.artist)
        assertEquals(audioSource1.albumId, audioSource2.albumId)
        assertEquals(audioSource1.album, audioSource2.album)
        assertEquals(audioSource1.genre, audioSource2.genre)
        assertEquals(audioSource1.year, audioSource2.year)
        assertEquals(audioSource1.duration, audioSource2.duration)
        assertEquals(audioSource1.trackNumber, audioSource2.trackNumber)
    }

    private val assertSongsEqual: (Song, Song) -> Unit = { song1, song2 ->
        assertEquals(song1.id, song2.id)
        assertEquals(song1.source, song2.source)

        assertEquals(song1.title, song2.title)
        assertEquals(song1.artistId, song2.artistId)
        assertEquals(song1.artist, song2.artist)
        assertEquals(song1.albumId, song2.albumId)
        assertEquals(song1.album, song2.album)
        assertEquals(song1.genre, song2.genre)
        assertEquals(song1.year, song2.year)
        assertEquals(song1.duration, song2.duration)
        assertEquals(song1.trackNumber, song2.trackNumber)
    }

    @Test
    fun test_metadataExtensions1() {

        val id = 371L
        val source = "Source"

        val audioType = AudioType.MUSIC
        val title = "Title"
        val artistId = 137L
        val artist = "Artist"
        val albumId = 1337L
        val album = "Album"
        val genre = "Genre"
        val year = 2003
        val duration = 3_000
        val trackNumber = 1

        val metadata: AudioMetadata = AudioSources.createMetadata(
            audioType, title, albumId, album, artistId, artist, genre, duration, year, trackNumber)

        val test: (AudioSource) -> Unit = { audioSource ->
            assertEquals(id, audioSource.id)
            assertEquals(source, audioSource.source)
            assertEquals(metadata, audioSource.metadata)

            assertEquals(title, audioSource.metadata.title)
            assertEquals(artistId, audioSource.metadata.artistId)
            assertEquals(artist, audioSource.metadata.artist)
            assertEquals(albumId, audioSource.metadata.albumId)
            assertEquals(album, audioSource.metadata.album)
            assertEquals(genre, audioSource.metadata.genre)
            assertEquals(year, audioSource.metadata.year)
            assertEquals(duration, audioSource.metadata.duration)
            assertEquals(trackNumber, audioSource.metadata.trackNumber)

            assertEquals(title, audioSource.title)
            assertEquals(artistId, audioSource.artistId)
            assertEquals(artist, audioSource.artist)
            assertEquals(albumId, audioSource.albumId)
            assertEquals(album, audioSource.album)
            assertEquals(genre, audioSource.genre)
            assertEquals(year, audioSource.year)
            assertEquals(duration, audioSource.duration)
            assertEquals(trackNumber, audioSource.trackNumber)
        }

        // test 1
        val original = AudioSources.createAudioSource(id, source, metadata)

        test.invoke(original)

        // test 2
        val copy = AudioSources.copyAudioSource(original)

        test.invoke(copy)

        // test 3
        val song = original.toSong()
        val original1 = song.toAudioSource()

        assertAudioSourcesEqual.invoke(original, original1)

    }

    @Test
    fun test_metadataExtensions2() {

        val songs = List(random.nextInt(10, 20)) { i ->
            mockSong(id = i.toLong())
        }

        val audioSources = songs.toAudioSources()

        val songs1 = audioSources.toSongs()

        for (i in songs.indices) {
            assertSongsEqual.invoke(songs[i], songs1[i])
        }

    }

    @Test
    fun test_metadataExtensions3() {

        val audioSources = List(random.nextInt(10, 20)) { i ->
            mockAudioSource(id = i.toLong())
        }

        val songs = audioSources.toSongs()

        val audioSources1 = songs.toAudioSources()

        for (i in audioSources.indices) {
            assertAudioSourcesEqual.invoke(audioSources[i], audioSources1[i])
        }

    }

}