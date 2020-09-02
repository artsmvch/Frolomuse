package com.frolo.muse.engine

import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.common.prepareByTarget
import com.frolo.muse.randomLong
import com.frolo.muse.randomString
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.absoluteValue
import junit.framework.TestCase.assertTrue


/**
 * Here are methods for testing [PlayerImpl.removeAll].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_RemoveAll_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_removeAll1() = doOnPlayerImpl { player ->
        // Test case 1: remove an audio source that comes before the current one in the queue

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val originalSize = queue.length

        val position = queue.length - 1

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        val firstItem = queue.getItemAt(0)

        player.removeAll(listOf(firstItem))

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), eq(position - 1))
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
        }
    }

    @Test
    fun test_removeAll2() = doOnPlayerImpl { player ->
        // Test case 2: remove an audio source that is current one (which is being played in the player)

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        player.removeAll(listOf(item))

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(queue.getItemAt(position)), eq(position))
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
        }
    }

    @Test
    fun test_removeAll3() = doOnPlayerImpl { player ->
        // Test case 3: remove an audio source after shuffling the queue in the player

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        player.setShuffleMode(Player.SHUFFLE_ON)

        player.waitUntilAllEventsAreOver()

        val currentQueue = player.getCurrentQueue()!!
        val currentPositionInQueue = player.getCurrentPositionInQueue()

        val victim = currentQueue.getItemAt(0)

        player.removeAll(listOf(victim))

        // Check if the victim is the current playing item
        if (item == victim) {
            player.doAfterAllEvents {
                verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(currentQueue.getItemAt(currentPositionInQueue)), eq(position))
                assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
            }
        } else {
            player.doAfterAllEvents {
                verify(testObserver, times(1)).onPositionInQueueChanged(same(player), eq(currentPositionInQueue))
                assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
            }
        }
    }

    @Test
    fun test_removeAll4() = doOnPlayerImpl { player ->
        // Test case 4: remove an audio source which has copies in the queue

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val list = getAudioSources(size = 2)

        val someItem = list[list.size - 1]
        val otherItem = list[list.size - 2]

        val newList = ArrayList<AudioSource>().apply {
            add(someItem)
            addAll(list)
            add(someItem)
        }

        val queue = AudioSourceQueue.create(
                AudioSourceQueue.NONE,
                randomLong().absoluteValue,
                randomString(),
                newList
        )
        val originalSize = queue.length

        val position = 0

        // item == someItem
        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        player.removeAll(listOf(someItem))

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(otherItem), eq(position))
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 3)
        }
    }

}