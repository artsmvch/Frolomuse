@file:Suppress("ClassName")

package com.frolo.muse.database

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.di.impl.local.PlaylistDatabaseManager
import com.frolo.muse.model.media.Song
import org.junit.Test
import org.junit.runner.RunWith
import java.com.frolo.muse.mockSongList


@RunWith(AndroidJUnit4::class)
class PlaylistDatabaseManager_Performance_Test {

    private fun currentTimestamp(): Long = System.currentTimeMillis()

    private fun obtainClearedPlaylistDatabaseManager(): PlaylistDatabaseManager {
        val context = InstrumentationRegistry.getTargetContext()
        return PlaylistDatabaseManager.get(context).apply { nuke().blockingAwait() }
    }

    private fun reportPerformance(count: Int, executionTime: Long, criticalExecutionTime: Long) {
        val message = "Database took $executionTime millis to add $count playlist members"
        if (executionTime >= criticalExecutionTime) {
            throw BadDatabasePerformanceException(message)
        } else {
            println(message)
        }
    }

    private fun test_Performance_addPlaylistMembers(count: Int, criticalTime: Long) {
        val manager = obtainClearedPlaylistDatabaseManager()
        val songs: List<Song> = mockSongList(size = count, allowIdCollisions = false)
        val playlist = manager.createPlaylist("TestPlaylist").blockingGet()
        val completable = manager.addPlaylistMembers(playlist.id, songs)
        val startTime = currentTimestamp()
        completable.blockingAwait()
        val endTime = currentTimestamp()
        val elapsedTime = endTime - startTime
        reportPerformance(count = count, executionTime = elapsedTime, criticalExecutionTime = criticalTime)
    }

    @Test
    fun test_Performance_addPlaylistMembers_SmallCount() {
        // 100 ms for 10 new members
        test_Performance_addPlaylistMembers(10, 100L)
    }

    @Test
    fun test_Performance_addPlaylistMembers_MediumCount() {
        // 5000 ms for 1_000 new members
        test_Performance_addPlaylistMembers(1_000, 5_000L)
    }

    @Test
    fun test_Performance_addPlaylistMembers_HugeCount() {
        // 150_000 ms for 3_0000 new members
        test_Performance_addPlaylistMembers(3_0000, 150_000L)
    }

//    @Test
//    fun test_Performance_addPlaylistMembers_InsaneCount() {
//        // 5_000_000 ms for 10_0000 new members
//        test_Performance_addPlaylistMembers(10_0000, 5_000_000L)
//    }

}