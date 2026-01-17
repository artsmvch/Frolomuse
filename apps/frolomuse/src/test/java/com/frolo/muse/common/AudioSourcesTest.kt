package com.frolo.muse.common

import com.frolo.player.AudioMetadata
import com.frolo.player.AudioSource
import com.frolo.player.data.AudioSources
import com.frolo.player.AudioType
import com.frolo.muse.mockAudioSource
import com.frolo.music.model.Song
import com.frolo.music.model.test.stubSong
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.random.Random


@RunWith(JUnit4::class)
class AudioSourcesTest {

    private val random = Random(System.currentTimeMillis())

    private val assertAudioSourcesEqual: (AudioSource, AudioSource) -> Unit = { audioSource1, audioSource2 ->
        assertEquals(audioSource1.getURI(), audioSource2.getURI())
        assertEquals(audioSource1.metadata, audioSource2.metadata)

        assertEquals(audioSource1.metadata.title, audioSource2.metadata.title)
        assertEquals(audioSource1.metadata.artistId, audioSource2.metadata.artistId)
        assertEquals(audioSource1.metadata.artist, audioSource2.metadata.artist)
        assertEquals(audioSource1.metadata.albumId, audioSource2.metadata.albumId)
        assertEquals(audioSource1.metadata.album, audioSource2.metadata.album)
        assertEquals(audioSource1.metadata.genre, audioSource2.metadata.genre)
        assertEquals(audioSource1.metadata.year, audioSource2.metadata.year)
        assertEquals(audioSource1.metadata.duration, audioSource2.metadata.duration)
        assertEquals(audioSource1.metadata.trackNumber, audioSource2.metadata.trackNumber)
    }

    private val assertSongsEqual: (Song, Song) -> Unit = { song1, song2 ->
        assertEquals(song1.getMediaId(), song2.getMediaId())

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
            assertEquals(source, audioSource.getURI())
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
        }

        // test 1
        val original = AudioSources.createAudioSource(source, metadata)

        test.invoke(original)

        // test 2
        val copy = AudioSources.copyAudioSource(original)

        test.invoke(copy)

        // test 3 - Skip asSong test since it now only works for Song instances
        // val song = original.asSong()
        // val original1 = song.toAudioSource()
        // assertAudioSourcesEqual.invoke(original, original1)

    }

    @Test
    fun test_metadataExtensions2() {

        val songs = List(random.nextInt(10, 20)) { i ->
            stubSong(id = i.toLong(), source = "source_$i")
        }

        val audioSources = songs.map { Util.createAudioSource(it) }

        val songs1 = audioSources.asSongs()

        for (i in songs.indices) {
            assertSongsEqual.invoke(songs[i], songs1[i])
        }

    }

    @Test
    fun test_metadataExtensions3() {

        val audioSources = List(random.nextInt(10, 20)) { i ->
            mockAudioSource(source = "source_$i")
        }

        val songs = audioSources.asSongs()

        val audioSources1 = songs.map { Util.createAudioSource(it) }

        for (i in audioSources.indices) {
            assertAudioSourcesEqual.invoke(audioSources[i], audioSources1[i])
        }

    }

}