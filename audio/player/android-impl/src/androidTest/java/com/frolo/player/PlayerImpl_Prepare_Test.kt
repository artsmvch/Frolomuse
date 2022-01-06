package com.frolo.player

import androidx.test.runner.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Here are methods for testing [PlayerImpl.prepareByTarget].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_Prepare_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_prepare() = doOnPlayerImpl { player ->

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = AudioSourceQueue.create(getAudioSources())

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

        // Start
        player.start()

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onPlaybackStarted(same(player))
        }

        // Pause
        player.pause()

        player.doAfterAllEvents {
            verify(testObserver, times(2)).onPlaybackPaused(same(player))
        }

        // Toggle 1
        player.toggle()

        player.doAfterAllEvents {
            verify(testObserver, times(2)).onPlaybackStarted(same(player))
        }

        // Toggle 2
        player.toggle()

        player.doAfterAllEvents {
            verify(testObserver, times(3)).onPlaybackPaused(same(player))
        }

    }

}