package com.frolo.muse.engine

import androidx.test.InstrumentationRegistry
import com.frolo.muse.common.toAudioSource
import com.frolo.muse.di.impl.local.SongRepositoryImpl
import com.frolo.muse.engine.stub.AudioFxStub
import com.frolo.muse.randomLong
import com.frolo.muse.randomString
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue


@Suppress("ClassName")
abstract class PlayerImpl_Base_Test {

    /**
     * Creates [PlayerImpl] instance for testing.
     */
    protected fun createPlayerImpl(): PlayerImpl {
        val context = InstrumentationRegistry.getTargetContext()
        return PlayerImpl.create(context, AudioFxStub) as PlayerImpl
    }

    /**
     * Queries all audio sources from the device.
     */
    protected fun getAudioSources(): List<AudioSource> {
        val context = InstrumentationRegistry.getTargetContext()
        val repository = SongRepositoryImpl(context)
        val songs = repository.allItems.blockingFirst()
        return songs.map { it.toAudioSource() }
    }

    protected fun createNonEmptyAudioSourceQueue(minSize: Int = 1): AudioSourceQueue {
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

    protected fun doOnPlayerImpl(block: (instance: PlayerImpl) -> Unit) {
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

}