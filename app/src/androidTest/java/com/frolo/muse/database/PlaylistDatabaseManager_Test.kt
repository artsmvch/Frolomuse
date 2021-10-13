@file:Suppress("ClassName")

package com.frolo.muse.database

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.TestLibraryConfiguration
import com.frolo.muse.di.impl.local.PlaylistDatabaseManager
import com.frolo.muse.di.impl.local.SongRepositoryImpl
import com.frolo.muse.kotlin.moveItem
import com.frolo.muse.model.media.Song
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PlaylistDatabaseManager_Test {

    private fun obtainNukedPlaylistDatabaseManager(): PlaylistDatabaseManager {
        val context = InstrumentationRegistry.getTargetContext()
        return PlaylistDatabaseManager.get(context).apply { nuke().blockingAwait() }
    }

    /**
     * Returns a list of song models of existing audio files.
     */
    private fun getSongList(count: Int): List<Song> {
        val context = InstrumentationRegistry.getTargetContext()
        val configuration = TestLibraryConfiguration(context)
        val repository = SongRepositoryImpl(configuration)
        val songs = repository.allItems.blockingFirst()
        if (songs.size < count) {
            throw IllegalStateException("Could not find enough songs: " +
                    "$count requested but ${songs.size} found only")
        }
        return songs.take(count)
    }

    private fun arePlayOrdersEqual(list1: List<Song>, list2: List<Song>): Boolean {
        if (list1.size != list2.size) {
            return false
        }
        for (i in list1.indices) {
            if (list1[i].id != list2[i].id) {
                return false
            }
        }
        return true
    }

    @Test
    fun test_movePlaylistMembers() {
        val manager = obtainNukedPlaylistDatabaseManager()
        val playlist = manager.createPlaylist("Playlist1").blockingGet()
        val originalSongs = getSongList(2)

        manager.addPlaylistMembers(playlist.id, originalSongs).blockingAwait()

        var resultList = manager.queryPlaylistMembers(playlist.id)
            .blockingFirst()
            .toMutableList()

        // Move pos 0 to pos 1
        manager.movePlaylistMember(resultList[0], resultList[1], null).blockingAwait()
        resultList.moveItem(0, 1)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }

        // Move pos 1 to pos 0
        manager.movePlaylistMember(resultList[1], null, resultList[0]).blockingAwait()
        resultList.moveItem(1, 0)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }

        // Move pos 0 to pos 1
        manager.movePlaylistMember(resultList[0], resultList[1], null).blockingAwait()
        resultList.moveItem(0, 1)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }
    }

    @Test
    fun test_addAndMoveAndRemovePlaylistMembers() {
        val manager = obtainNukedPlaylistDatabaseManager()
        val playlist = manager.createPlaylist("Playlist2").blockingGet()
        val originalSongs = getSongList(10)

        manager.addPlaylistMembers(playlist.id, originalSongs).blockingAwait()

        var resultList = manager.queryPlaylistMembers(playlist.id)
            .blockingFirst()
            .toMutableList()

        // Move pos 0 to pos 9
        manager.movePlaylistMember(resultList[0], resultList[9], null).blockingAwait()
        resultList.moveItem(0, 9)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }

        // Move pos 0 to pos 8
        manager.movePlaylistMember(resultList[0], resultList[8], resultList[9]).blockingAwait()
        resultList.moveItem(0, 8)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }

        // Remove pos 8
        manager.removePlaylistMember(playlist.id, resultList[8]).blockingAwait()
        var removed = resultList.removeAt(8)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }

        // Move pos 1 to pos 0
        manager.movePlaylistMember(resultList[1], null, resultList[0]).blockingAwait()
        resultList.moveItem(1, 0)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }

        // Add the previously removed
        manager.addPlaylistMember(playlist.id, removed).blockingAwait()
        resultList.add(removed)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }

        // Move pos 9 to pos 8
        manager.movePlaylistMember(resultList[9], resultList[7], resultList[8]).blockingAwait()
        resultList.moveItem(9, 8)

        manager.queryPlaylistMembers(playlist.id).blockingFirst().also { updatedList ->
            assertTrue(arePlayOrdersEqual(updatedList, resultList))
            resultList = updatedList.toMutableList()
        }
    }

}