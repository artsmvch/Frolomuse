package com.frolo.player

import androidx.test.runner.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith
import junit.framework.TestCase.assertTrue


/**
 * Here are methods for testing [PlayerImpl.removeAt].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_RemoveAt_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_removeAt1() = doOnPlayerImpl { player ->
        // Test case 1: remove item at the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val originalQueue = createNonEmptyAudioSourceQueue(size = 2)
        val queue = originalQueue.createCopy()
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        player.removeAt(position)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(originalQueue.getItemAt(position + 1)), eq(position))
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
        }
    }

    @Test
    fun test_removeAt2() = doOnPlayerImpl { player ->
        // Test case 2: remove item at a position that comes before the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val originalQueue = createNonEmptyAudioSourceQueue(size = 2)
        val queue = originalQueue.createCopy()
        val originalSize = queue.length

        val position = 1

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        player.removeAt(0)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), eq(0))
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
        }
    }

    @Test
    fun test_removeAt3() = doOnPlayerImpl { player ->
        // Test case 3: remove item at a position that comes after the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val originalQueue = createNonEmptyAudioSourceQueue(size = 2)
        val queue = originalQueue.createCopy()
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        player.removeAt(1)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(0))
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
        }
    }

}