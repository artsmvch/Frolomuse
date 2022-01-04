package com.frolo.player

import androidx.test.runner.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Here are methods for testing [PlayerImpl.skipToPrevious].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_SkipToPrevious_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_skipToPrevious1() = doOnPlayerImpl { player ->

        // Test case 1: prepare a queue with the first item as the target, do not start the playback, skip to the previous

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val position = 0
        val item = queue.getItemAt(position)

        player.prepareByTarget(queue, item, false)

        player.skipToPrevious()

        player.doAfterAllEvents {
            val p = queue.length - 1
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(queue.getItemAt(p)), eq(p))
            verify(testObserver, times(2)).onPlaybackPaused(same(player))
        }

    }

    @Test
    fun test_skipToPrevious2() = doOnPlayerImpl { player ->

        // Test case 2: prepare a queue with the first item as the target, start the playback and wait until it's complete, skip to the previous

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val position = 0
        val item = queue.getItemAt(position)

        player.prepareByTarget(queue, item, true)

        player.simulateCompletePlayback()

        player.skipToPrevious()

        player.doAfterAllEvents {
            val p = position
            verify(testObserver, times(2)).onAudioSourceChanged(same(player), eq(queue.getItemAt(p)), eq(p))
            verify(testObserver, times(3)).onPlaybackStarted(same(player))
        }

    }

    @Test
    fun test_skipToPrevious3() = doOnPlayerImpl { player ->

        // Test case 3: prepare a queue with the first item as the target, shuffle the queue, skip to the previous

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(size = 2)
        val position = 0
        val item = queue.getItemAt(position)

        player.prepareByTarget(queue, item, false)

        player.setShuffleMode(Player.SHUFFLE_ON)

        player.skipToPrevious()

        player.doAfterAllEvents {
            val currentQueue = player.getCurrentQueue()!!
            val p = currentQueue.indexOf(item)
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(queue.getItemAt(p)), eq(p))
            verify(testObserver, times(2)).onPlaybackPaused(same(player))
        }

    }

}