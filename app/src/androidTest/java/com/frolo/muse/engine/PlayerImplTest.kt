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
import kotlin.math.absoluteValue


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

    private fun createNonEmptyAudioSourceQueue(minSize: Int = 1): AudioSourceQueue {
        val queue = AudioSourceQueue.create(
            AudioSourceQueue.NONE,
            randomLong(),
            randomString(),
            getAudioSources()
        )

        if (queue.length < minSize) {
            // it's undefined state
            throw IllegalStateException("There are no enough songs in the device, requested at least $minSize but found only ${queue.length}")
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
    fun test_prepare() = doOnPlayerImpl { player ->

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
        player.simulateCompletePlayback()

        player.doAfterAllEvents {
            verify(testObserver, times(2)).onAudioSourceChanged(same(player), eq(item), eq(position))
        }

        // Set REPEAT_PLAYLIST
        player.setRepeatMode(Player.REPEAT_PLAYLIST)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onRepeatModeChanged(same(player), eq(Player.REPEAT_PLAYLIST))
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
        }

    }

    @Test
    fun test_removeAll1() = doOnPlayerImpl { player ->
        // Test case 1: remove an audio source that comes before the current one in the queue

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val queue = createNonEmptyAudioSourceQueue(minSize = 2)
        val originalSize = queue.length

        val position = queue.length - 1

        val item = queue.getItemAt(position)

        // Prepare
        player.prepare(queue, item, false)

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

        val queue = createNonEmptyAudioSourceQueue(minSize = 2)
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepare(queue, item, false)

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

        val queue = createNonEmptyAudioSourceQueue(minSize = 2)
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepare(queue, item, false)

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

        val list = getAudioSources()

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
        player.prepare(queue, item, false)

        player.removeAll(listOf(someItem))

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(otherItem), eq(position))
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 3)
        }
    }

    @Test
    fun test_removeAt1() = doOnPlayerImpl { player ->
        // Test case 1: remove item at the current playing position

        val testObserver = mock<TestPlayerObserver>()
        player.registerObserver(testObserver)

        val originalQueue = createNonEmptyAudioSourceQueue(minSize = 2)
        val queue = originalQueue.clone()
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepare(queue, item, false)

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

        val originalQueue = createNonEmptyAudioSourceQueue(minSize = 2)
        val queue = originalQueue.clone()
        val originalSize = queue.length

        val position = 1

        val item = queue.getItemAt(position)

        // Prepare
        player.prepare(queue, item, false)

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

        val originalQueue = createNonEmptyAudioSourceQueue(minSize = 2)
        val queue = originalQueue.clone()
        val originalSize = queue.length

        val position = 0

        val item = queue.getItemAt(position)

        // Prepare
        player.prepare(queue, item, false)

        player.removeAt(1)

        player.doAfterAllEvents {
            verify(testObserver, times(1)).onAudioSourceChanged(same(player), eq(item), eq(0))
            verify(testObserver, never()).onPositionInQueueChanged(same(player), any())
            assertTrue(player.getCurrentQueue()!!.length == originalSize - 1)
        }
    }

}