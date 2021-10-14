package com.frolo.muse.engine

import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.common.prepareByTarget
import com.frolo.muse.randomEnumValue
import com.frolo.muse.randomInt
import com.frolo.muse.randomLong
import com.frolo.muse.randomString
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith
import junit.framework.TestCase.assertTrue


/**
 * Here are methods for testing [PlayerImpl.update].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_Update_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_update1() = doOnPlayerImpl { player ->
        // Test case 1: update an item that IS NOT the current one

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        val secondItem = queue.getItemAt(1)
        val newSecondItemMetadata = AudioSources.createMetadata(
            randomEnumValue(),
            randomString(),
            randomLong(),
            randomString(),
            randomLong(),
            randomString(),
            randomString(),
            randomInt(),
            randomInt(),
            randomInt()
        )
        val updatedSecondItem = AudioSources.createAudioSource(
            secondItem.id,
            secondItem.source,
            newSecondItemMetadata
        )

        player.update(updatedSecondItem)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, never()).onAudioSourceUpdated(same(player), any())
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentQueue()!!.length == originalSize)
        }
    }

    @Test
    fun test_update2() = doOnPlayerImpl { player ->
        // Test case 1: update an item that IS the current one

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        // Wait until all events are over to make sure [onAudioSourceChanged] is called after the preparation
        player.waitUntilAllEventsAreOver()

        val newItemMetadata = AudioSources.createMetadata(
            randomEnumValue(),
            randomString(),
            randomLong(),
            randomString(),
            randomLong(),
            randomString(),
            randomString(),
            randomInt(),
            randomInt(),
            randomInt()
        )
        val updatedItem = AudioSources.createAudioSource(
            item.id,
            item.source,
            newItemMetadata
        )

        player.update(updatedItem)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onAudioSourceUpdated(same(player), eq(updatedItem))
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentQueue()!!.length == originalSize)
        }
    }

}