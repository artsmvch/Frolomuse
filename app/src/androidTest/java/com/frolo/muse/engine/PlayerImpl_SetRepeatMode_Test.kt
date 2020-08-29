package com.frolo.muse.engine

import androidx.test.runner.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Here are methods for testing [PlayerImpl.setRepeatMode].
 */
@Suppress("ClassName")
@RunWith(AndroidJUnit4::class)
class PlayerImpl_SetRepeatMode_Test : PlayerImpl_Base_Test() {

    @Test
    fun test_setRepeatMode() = doOnPlayerImpl { player ->

        val testObserver = mock<TestPlayerObserver>()

        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue()

        val position = queue.length - 1

        val item = queue.getItemAt(position)

        // Prepare
        player.prepare(queue, item, true)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onQueueChanged(same(player), argThat(AudioSourceQueueEquals(queue)))
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(position))
            verify(testObserver, times(1)).onPlaybackStarted(same(player))
        }

        // Set REPEAT_ONE
        player.setRepeatMode(Player.REPEAT_ONE)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onRepeatModeChanged(same(player), eq(Player.REPEAT_ONE))
            TestCase.assertTrue(player.getRepeatMode() == Player.REPEAT_ONE)
        }

        // Rewind to the end
        player.simulateCompletePlayback()

        player.doAfterAllEvents {
            verify(testObserver, times(2)).onAudioSourceChanged(same(player), eq(item), eq(position))
        }

        // Set REPEAT_PLAYLIST
        player.setRepeatMode(Player.REPEAT_PLAYLIST)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onRepeatModeChanged(same(player), eq(Player.REPEAT_PLAYLIST))
            TestCase.assertTrue(player.getRepeatMode() == Player.REPEAT_PLAYLIST)
        }

        // Rewind to the end
        player.simulateCompletePlayback()

        player.doAfterAllEvents {
            if (queue.length == 1) {
                verify(testObserver, times(3)).onAudioSourceChanged(same(player), eq(item), eq(position))
            } else {
                val p = 0
                verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(queue.getItemAt(p)), eq(p))
            }
        }

        // Set REPEAT_OFF
        player.setRepeatMode(Player.REPEAT_OFF)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onRepeatModeChanged(same(player), eq(Player.REPEAT_OFF))
            TestCase.assertTrue(player.getRepeatMode() == Player.REPEAT_OFF)
        }

    }

}