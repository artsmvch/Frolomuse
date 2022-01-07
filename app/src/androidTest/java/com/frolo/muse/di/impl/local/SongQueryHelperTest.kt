package com.frolo.muse.di.impl.local

import android.provider.MediaStore.Audio.Media.*
import com.frolo.muse.OS
import com.frolo.muse.model.media.SongFeatures
import com.frolo.music.model.SongFilter
import com.frolo.music.model.SongType
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class SongQueryHelperTest {

    // All enabled
    @Test
    fun test_GetSelectionWithArgs1() {
        val filter = SongFilter.allEnabled()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection: String? = null
        val expectedArgs: Array<String>? = null
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Name piece
    @Test
    fun test_GetSelectionWithArgs2() {
        val filter = SongFilter.Builder()
            .setNamePiece("test")
            .setOnlyTypes(SongType.ALARM)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_ALARM != ?) AND $TITLE LIKE ?"
        val expectedArgs = arrayOf<String>("0", "%test%")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Album
    @Test
    fun test_GetSelectionWithArgs3() {
        val filter = SongFilter.Builder()
            .setOnlyType(SongType.MUSIC)
            .setAlbumId(1337)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_MUSIC != ?) AND $ALBUM_ID = ?"
        val expectedArgs = arrayOf<String>("0", "1337")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Artist
    @Test
    fun test_GetSelectionWithArgs4() {
        val filter = SongFilter.Builder()
            .setOnlyTypes(setOf(SongType.RINGTONE, SongType.NOTIFICATION))
            .setArtistId(3751)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_RINGTONE != ? OR $IS_NOTIFICATION != ?) AND $ARTIST_ID = ?"
        val expectedArgs = arrayOf<String>("0", "0", "3751")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Genre
    @Test
    fun test_GetSelectionWithArgs5() {
        val filter = SongFilter.Builder()
            .setOnlyType(SongType.RINGTONE)
            .setGenreId(3001)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_RINGTONE != ?) AND $GENRE_ID = ?"
        val expectedArgs = arrayOf<String>("0", "3001")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Min duration
    @Test
    fun test_GetSelectionWithArgs6() {
        val filter = SongFilter.Builder()
            .setOnlyTypes(setOf(SongType.ALARM, SongType.MUSIC))
            .setMinDuration(101)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_MUSIC != ? OR $IS_ALARM != ?) AND $DURATION >= ?"
        val expectedArgs = arrayOf<String>("0", "0", "101")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Min duration is 0
    @Test
    fun test_GetSelectionWithArgs7() {
        val filter = SongFilter.Builder()
            .setOnlyTypes(setOf(SongType.ALARM, SongType.MUSIC))
            .setMinDuration(0)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_MUSIC != ? OR $IS_ALARM != ?)"
        val expectedArgs = arrayOf<String>("0", "0")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Folder path
    @Test
    fun test_GetSelectionWithArgs8() {
        val filter = SongFilter.Builder()
            .setFolderPath("/root/music")
            .setOnlyTypes(SongType.PODCAST, SongType.ALARM)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_PODCAST != ? OR $IS_ALARM != ?) AND $DATA LIKE ?"
        val expectedArgs = arrayOf<String>("0", "0", "%/root/music/%")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Filepath
    @Test
    fun test_GetSelectionWithArgs9() {
        val filter = SongFilter.Builder()
            .setFilepath("/root/music/nirvana.mp3")
            .setOnlyTypes(SongType.NOTIFICATION, SongType.MUSIC, SongType.ALARM)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_MUSIC != ? OR $IS_ALARM != ? OR $IS_NOTIFICATION != ?) AND $DATA LIKE ?"
        val expectedArgs = arrayOf<String>("0", "0", "0", "/root/music/nirvana.mp3")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Folder and filepath
    @Test
    fun test_GetSelectionWithArgs10() {
        val filter = SongFilter.Builder()
            .setOnlyTypes(SongType.NOTIFICATION)
            .setFilepath("/root/downloads")
            .setFilepath("/root/music/pilots.mp3")
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection = "($IS_NOTIFICATION != ?) AND $DATA LIKE ?"
        val expectedArgs = arrayOf<String>("0", "/root/music/pilots.mp3")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // All enabled (but in another way)
    @Test
    fun test_GetSelectionWithArgs11() {
        val filter = SongFilter.Builder().build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection: String? = null
        val expectedArgs: Array<String>? = null
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // All song types enabled, but name piece
    @Test
    fun test_GetSelectionWithArgs12() {
        val filter = SongFilter.Builder()
            .allTypes()
            .setNamePiece("piece_of_@")
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection: String = "$TITLE LIKE ?"
        val expectedArgs: Array<String> = arrayOf("%piece_of_@%")
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Supported song types
    @Test
    fun test_GetSelectionWithArgs13() {
        val supportedSongTypes = SongType.values().filter { SongFeatures.isSongTypeSupported(it) }
        val filter = SongFilter.Builder()
            .setOnlyTypes(supportedSongTypes)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection: String? = null
        val expectedArgs: Array<String>? = null
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // Unsupported song types
    @Test
    fun test_GetSelectionWithArgs14() {
        val unsupportedSongTypes = SongType.values().filterNot { SongFeatures.isSongTypeSupported(it) }
        val filter = SongFilter.Builder()
            .setOnlyTypes(unsupportedSongTypes)
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection: String?
        val expectedArgs: Array<String>?
        if (OS.isAtLeastQ()) {
            expectedSelection = "($IS_MUSIC = ? AND $IS_PODCAST = ? AND $IS_RINGTONE = ? " +
                    "AND $IS_ALARM = ? AND $IS_NOTIFICATION = ? AND $IS_AUDIOBOOK = ?)"
            expectedArgs = arrayOf<String>("0", "0", "0", "0", "0", "0")
        } else {
            expectedSelection = "($IS_MUSIC = ? AND $IS_PODCAST = ? AND $IS_RINGTONE = ? " +
                    "AND $IS_ALARM = ? AND $IS_NOTIFICATION = ?)"
            expectedArgs = arrayOf<String>("0", "0", "0", "0", "0")
        }
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

    // No types enabled
    @Test
    fun test_GetSelectionWithArgs15() {
        val filter = SongFilter.Builder()
            .noTypes()
            .build()
        val selectionWithArgs = SongQueryHelper.getSelectionWithArgs(filter)
        val expectedSelection: String?
        val expectedArgs: Array<String>?
        if (OS.isAtLeastQ()) {
            expectedSelection = "($IS_MUSIC = ? AND $IS_PODCAST = ? AND $IS_RINGTONE = ? " +
                    "AND $IS_ALARM = ? AND $IS_NOTIFICATION = ? AND $IS_AUDIOBOOK = ?)"
            expectedArgs = arrayOf<String>("0", "0", "0", "0", "0", "0")
        } else {
            expectedSelection = "($IS_MUSIC = ? AND $IS_PODCAST = ? AND $IS_RINGTONE = ? " +
                    "AND $IS_ALARM = ? AND $IS_NOTIFICATION = ?)"
            expectedArgs = arrayOf<String>("0", "0", "0", "0", "0")
        }
        assertEquals(selectionWithArgs.selection, expectedSelection)
        assertArrayEquals(selectionWithArgs.args, expectedArgs)
    }

}