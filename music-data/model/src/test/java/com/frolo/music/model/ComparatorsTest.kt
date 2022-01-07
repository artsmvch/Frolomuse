package com.frolo.music.model

import com.frolo.music.model.test.mockSong
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ComparatorsTest {

    @Test
    fun test_compareSongsByTrackNumber() {

        val song1 = mockSong(id = 1, trackNumber = 100)
        val song2 = mockSong(id = 2, trackNumber = 10)
        val song3 = mockSong(id = 3, trackNumber = 30)
        val song4 = mockSong(id = 4, trackNumber = 9)
        val song5 = mockSong(id = 5, trackNumber = 5)
        val song6 = mockSong(id = 6, trackNumber = 101)
        val song7 = mockSong(id = 7, trackNumber = 25)
        val song8 = mockSong(id = 8, trackNumber = 1)
        val song9 = mockSong(id = 9, trackNumber = 15)
        val song10 = mockSong(id = 10, trackNumber = 10)

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