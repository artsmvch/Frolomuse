package com.frolo.player

import android.Manifest
import androidx.test.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import androidx.test.rule.GrantPermissionRule

import org.junit.Rule


@Suppress("ClassName")
abstract class PlayerImpl_Base_Test {

    @get:Rule
    val runtimePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)

    /**
     * Creates [PlayerImpl] instance for testing.
     */
    protected fun createPlayerImpl(): PlayerImpl {
        val context = InstrumentationRegistry.getTargetContext()
        return PlayerImpl.newBuilder(context)
            .setUseWakeLocks(false)
            .build()
    }

    /**
     * Queries all audio sources from the device.
     */
    protected fun getAudioSources(size: Int? = null): List<AudioSource> {
        if (size != null && size < 0) {
            throw IllegalArgumentException("Invalid size = $size")
        }
        val context = InstrumentationRegistry.getTargetContext()
        val factory = AudioSourceFactory(context)
        val audioSources = factory.getList(size)
        if (size != null) {
            if (size > audioSources.size) {
                // it's undefined state
                throw IllegalStateException("There are no enough songs in the device, requested $size but found only ${audioSources.size}")
            }
            return audioSources.subList(0, size)
        }
        return audioSources
    }

    protected fun createNonEmptyAudioSourceQueue(size: Int? = null): AudioSourceQueue {
        return AudioSourceQueue.create(getAudioSources(size))
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
        assertTrue(player.isShutdown())
    }

}