package com.frolo.muse.engine

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.di.impl.local.SongRepositoryImpl
import com.frolo.muse.engine.stub.AudioFxStub
import com.frolo.muse.randomLong
import com.frolo.muse.randomString
import com.nhaarman.mockitokotlin2.*
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PlayerImplTest {

    /**
     * Creates [PlayerImpl] instance for testing.
     */
    private fun createPlayerImpl(): PlayerImpl {
        val context = InstrumentationRegistry.getTargetContext()
        return PlayerImpl.create(context, AudioFxStub) as PlayerImpl
    }

    /**
     * Queries all audio sources from the device.
     */
    private fun getAudioSources(): List<AudioSource> {
        val context = InstrumentationRegistry.getTargetContext()
        val repository = SongRepositoryImpl(context)
        val songs = repository.allItems.blockingFirst()
        return songs.map { it.toAudioSource() }
    }

    private fun createNonEmptyAudioSourceQueue(): AudioSourceQueue {
        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            getAudioSources()
        )

        if (queue.isEmpty) {
            // it's undefined state
            throw IllegalStateException("There are no songs in the device")
        }

        return queue
    }

    private fun doOnPlayerImpl(block: (instance: PlayerImpl) -> Unit) {
        val observer = mock<TestPlayerObserver>()
        val player = createPlayerImpl()
        player.registerObserver(observer)
        // Client's block
        block.invoke(player)
        player.shutdown()
        player.postOnEventThread(false) {
            verify(observer, times(1)).onShutdown(same(player))
        }
        assertFalse(player.isPrepared())
        assertFalse(player.isPlaying())
        assertTrue(player.isShutdown)
    }

    @Test
    fun test_prepare() {

        val player = createPlayerImpl()

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
        player.prepare(queue, item, false)

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

    @Test
    fun test_shuffle() {

        val player = createPlayerImpl()

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
        player.prepare(queue, item, false)

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
        }

        // Un-shuffle
        val pBefore = player.getCurrentPositionInQueue()
        player.setShuffleMode(Player.SHUFFLE_OFF)

        player.doAfterAllEvents {
            val p = player.getCurrentQueue()?.indexOf(item) ?: -1
            verify(testObserver, times(if (p == pBefore) 2 else 1)).onPositionInQueueChanged(same(player), eq(p))
            verify(testObserver, times(1)).onShuffleModeChanged(same(player), eq(Player.SHUFFLE_OFF))
        }

    }

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
        }

        // Rewind to the end
        player.seekTo(player.getDuration())
        player.awaitPlaybackCompletion()

        player.doAfterAllEvents {
            verify(testObserver, times(2)).onAudioSourceChanged(same(player), eq(item), eq(position))
        }

        // Set REPEAT_PLAYLIST
        player.setRepeatMode(Player.REPEAT_PLAYLIST)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onRepeatModeChanged(same(player), eq(Player.REPEAT_PLAYLIST))
        }

        // Rewind to the end
        player.seekTo(player.getDuration())
        player.awaitPlaybackCompletion()

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
        }

    }

}