package com.frolo.muse.engine

import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.common.prepareByTarget
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import junit.framework.TestCase.assertTrue


/**
 * Here are methods for testing [PlayerImpl.addAll].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_AddAll_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_addAll1() = doOnPlayerImpl { player ->
        // Test case 1: add a list of audio sources when the player has a non-empty queue

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val originalSize = queue.length

        val position = queue.length - 1

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        // Wait until all events are over to make sure [onAudioSourceChanged] is called after the preparation
        player.waitUntilAllEventsAreOver()

        val listToAdd = queue.snapshot.toList()

        player.addAll(listToAdd)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), any())
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentQueue()!!.length == originalSize + listToAdd.size)

            val currentQueue = player.getCurrentQueue()!!
            val currQueueLength = currentQueue.length
            for (i in listToAdd.indices) {
                assertEquals(currentQueue.getItemAt(currQueueLength - 1 - i), listToAdd[listToAdd.size - 1 - i])
            }
        }
    }

    @Test
    fun test_addAll2() = doOnPlayerImpl { player ->
        // Test case 2: add a list of audio sources when the player has an empty queue

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = AudioSourceQueue.empty()
        val originalSize = queue.length

        val listToAdd = getAudioSources()
        if (listToAdd.isEmpty()) {
            throw IllegalStateException()
        }

        // Prepare
        player.prepareByTarget(queue, listToAdd.first(), false)

        // Wait until all events are over to make sure [onAudioSourceChanged] is called after the preparation
        player.waitUntilAllEventsAreOver()

        player.addAll(listToAdd)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), any())
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(null), eq(-1))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(listToAdd.first()), eq(0))
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentQueue()!!.length == originalSize + listToAdd.size)

            val currentQueue = player.getCurrentQueue()!!
            for (i in listToAdd.indices) {
                assertEquals(currentQueue.getItemAt(i), listToAdd[i])
            }
        }
    }

}