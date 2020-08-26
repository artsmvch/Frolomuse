package com.frolo.muse.engine

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.di.impl.local.SongRepositoryImpl
import com.frolo.muse.engine.stub.AudioFxStub
import com.frolo.muse.randomLong
import com.frolo.muse.randomString
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PlayerImplTest {

    /**
     * Creates [PlayerImpl] instance for testing.
     */
    private fun getPlayerImpl(): PlayerImpl {
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

    @Test
    fun test_prepare() {

        val player = getPlayerImpl()

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

}