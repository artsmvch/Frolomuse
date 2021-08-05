package com.frolo.muse.engine

import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.common.prepareByTarget
import java.com.frolo.muse.randomLong
import java.com.frolo.muse.randomString
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Here are methods for testing [PlayerImpl.setShuffleMode].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_SetShuffleMode_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_shuffle() = doOnPlayerImpl { player ->

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            getAudioSources()
        )

        if (queue.isEmpty) {
            // it's undefined state
            throw IllegalStateException()
        }

        val position = queue.length / 2 // somewhere in the middle
        val item = queue.getItemAt(position)

        // Prepare
        player.prepareByTarget(queue, item, false)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), argThat(AudioSourceQueueEquals(queue)))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onPlaybackPaused(same(player))
        }

        // Shuffle
        player.setShuffleMode(Player.SHUFFLE_ON)

        player.doAfterAllEvents {
            val p = player.getCurrentQueue()?.indexOf(item) ?: -1
            verify(testObserver, times(1)).onPositionInQueueChanged(same(player), eq(p))
            verify(testObserver, times(1)).onShuffleModeChanged(same(player), eq(Player.SHUFFLE_ON))
            assertTrue(player.getShuffleMode() == Player.SHUFFLE_ON)
        }

        // Un-shuffle
        val pBefore = player.getCurrentPositionInQueue()
        player.setShuffleMode(Player.SHUFFLE_OFF)

        player.doAfterAllEvents {
            val p = player.getCurrentQueue()?.indexOf(item) ?: -1
            verify(testObserver, times(if (p == pBefore) 2 else 1)).onPositionInQueueChanged(same(player), eq(p))
            verify(testObserver, times(1)).onShuffleModeChanged(same(player), eq(Player.SHUFFLE_OFF))
            assertTrue(player.getShuffleMode() == Player.SHUFFLE_OFF)
        }

    }

}