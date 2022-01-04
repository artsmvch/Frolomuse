package com.frolo.player

import androidx.test.runner.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Here are methods for testing [PlayerImpl.skipTo].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_SkipTo_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_skipTo1() = doOnPlayerImpl { player ->

        // Test case 1: prepare a queue with the first item as the target, do not start the playback, skip to the second and force the playback start

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val position = 0
        val item = queue.getItemAt(position)

        player.prepareByTarget(queue, item, false)

        player.skipTo(position + 1, true)

        player.doAfterAllEvents {
            val p = position + 1
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(queue.getItemAt(p)), eq(p))
            verify(testObserver, times(1)).onPlaybackPaused(same(player))
            verify(testObserver, times(1)).onPlaybackStarted(same(player))
            assertTrue(player.isPrepared())
            assertTrue(player.isPlaying())
        }

    }

    @Test
    fun test_skipTo2() = doOnPlayerImpl { player ->

        // Test case 2: prepare a queue with the last item as the target, skip to the same position and force the playback start

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val position = queue.length - 1
        val item = queue.getItemAt(position)

        player.prepareByTarget(queue, item, false)

        player.skipTo(position, true)

        player.doAfterAllEvents {
            // it should be called only once, since the position was not changed
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(queue.getItemAt(position)), eq(position))
            verify(testObserver, times(1)).onPlaybackPaused(same(player))
            verify(testObserver, times(1)).onPlaybackStarted(same(player))
            assertTrue(player.isPrepared())
            assertTrue(player.isPlaying())
        }

    }

}