package com.frolo.muse.engine

import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.common.prepareByPosition
import com.frolo.muse.randomLong
import com.frolo.muse.randomString
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Here are methods for testing [PlayerImpl.moveItem].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_MoveItem_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_moveItem1() = doOnPlayerImpl { player ->

        // Test case 1: move an item from pos1 to pos2, pos1 and pos2 come before the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val list = getAudioSources().let { it + it }
        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            list
        )

        if (queue.isEmpty) {
            throw IllegalStateException()
        }

        val position = 2
        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByPosition(queue, position, false)

        player.waitUntilAllEventsAreOver()

        player.moveItem(0, 1)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), argThat(AudioSourceQueueEquals(queue)))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), any(), any())
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentPositionInQueue() == position)
        }

    }

    @Test
    fun test_moveItem2() = doOnPlayerImpl { player ->

        // Test case 2: move an item from pos1 to pos2, pos1 and pos2 come after the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val list = getAudioSources().let { it + it }
        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            list
        )

        if (queue.isEmpty) {
            throw IllegalStateException()
        }

        val position = 0
        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByPosition(queue, position, false)

        player.waitUntilAllEventsAreOver()

        player.moveItem(1, 2)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), argThat(AudioSourceQueueEquals(queue)))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), any(), any())
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentPositionInQueue() == position)
        }

    }

    @Test
    fun test_moveItem3() = doOnPlayerImpl { player ->

        // Test case 3: move an item from pos1 to pos2, pos1 comes before the current playing position, and pos2 comes after the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val list = getAudioSources().let { it + it }
        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            list
        )

        if (queue.isEmpty) {
            throw IllegalStateException()
        }

        val position = 1
        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByPosition(queue, position, false)

        player.waitUntilAllEventsAreOver()

        player.moveItem(0, 2)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), argThat(AudioSourceQueueEquals(queue)))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), any(), any())
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), eq(0))
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentPositionInQueue() == 0)
        }

    }

    @Test
    fun test_moveItem4() = doOnPlayerImpl { player ->

        // Test case 4: move an item from pos1 to pos2, pos1 is the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val list = getAudioSources().let { it + it }
        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            list
        )

        if (queue.isEmpty) {
            throw IllegalStateException()
        }

        val position = 0
        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByPosition(queue, position, false)

        player.waitUntilAllEventsAreOver()

        player.moveItem(0, 2)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), argThat(AudioSourceQueueEquals(queue)))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), any(), any())
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), eq(2))
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentPositionInQueue() == 2)
        }

    }

    @Test
    fun test_moveItem5() = doOnPlayerImpl { player ->

        // Test case 5: move an item from pos1 to pos2, pos2 is the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val list = getAudioSources().let { it + it }
        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            list
        )

        if (queue.isEmpty) {
            throw IllegalStateException()
        }

        val position = 2
        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByPosition(queue, position, false)

        player.waitUntilAllEventsAreOver()

        player.moveItem(0, 2)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), argThat(AudioSourceQueueEquals(queue)))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), any(), any())
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), eq(1))
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentPositionInQueue() == 1)
        }

    }

}