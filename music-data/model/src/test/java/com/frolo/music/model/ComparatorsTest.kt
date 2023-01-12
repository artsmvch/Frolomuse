package com.frolo.music.model

import com.frolo.music.model.test.stubSong
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComparatorsTest {

    @Test
    fun test_compareSongsByTrackNumber() {

        val song1 = stubSong(id = 1, trackNumber = 100)
        val song2 = stubSong(id = 2, trackNumber = 10)
        val song3 = stubSong(id = 3, trackNumber = 30)
        val song4 = stubSong(id = 4, trackNumber = 9)
        val song5 = stubSong(id = 5, trackNumber = 5)
        val song6 = stubSong(id = 6, trackNumber = 101)
        val song7 = stubSong(id = 7, trackNumber = 25)
        val song8 = stubSong(id = 8, trackNumber = 1)
        val song9 = stubSong(id = 9, trackNumber = 15)
        val song10 = stubSong(id = 10, trackNumber = 10)

        // Ta-dah
        val unsortedSongs = listOf<Song>(
            song1, song2, song3, song4, song5, song6, song7, song8, song9, song10
        )

        // Sorted
        val actualSortedSongs = unsortedSongs.sortedWith(SongComparators.BY_TRACK_NUMBER)

        // Expected
        val expectedSortedSongs = listOf<Song>(
            song8, song5, song4, song2, song10, song9, song7, song3, song1, song6
        )

        assertEquals(actualSortedSongs, expectedSortedSongs)
    }

}