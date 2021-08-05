@file:Suppress("ClassName")

package com.frolo.muse.database

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.di.impl.local.PlaylistDatabaseManager
import com.frolo.muse.model.media.Song
import org.junit.Test
import org.junit.runner.RunWith
import java.com.frolo.muse.mockList


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
        val songs: List<Song> = mockList(size = count)
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
        test_Performance_addPlaylistMembers(10, 10L)
    }

    @Test
    fun test_Performance_addPlaylistMembers_MediumCount() {
        test_Performance_addPlaylistMembers(1000, 1000L)
    }

    @Test
    fun test_Performance_addPlaylistMembers_HugeCount() {
        test_Performance_addPlaylistMembers(10000, 10000L)
    }

}