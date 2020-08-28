package com.frolo.muse.engine

import java.util.concurrent.CountDownLatch
import kotlin.math.max


/**
 * Convenient Kotlin extension for [PlayerImpl.postOnEventThread].
 */
fun PlayerImpl.postOnEventThread(delay: Boolean, action: () -> Unit): Unit = postOnEventThread(action, delay)

/**
 * Waits until all pending and potential events perform and then executes [action].
 * NOTE: the method is blocking.
 * This can be useful if you want to test a player observer
 * so that you can ensure that events are performed before you act on the observer.
 */
fun PlayerImpl.doAfterAllEvents(action: () -> Unit) {
    val countDownLatch = CountDownLatch(1)
    val actionWrapper = Runnable {
        action.invoke()
        countDownLatch.countDown()
    }
    postOnEventThread(actionWrapper, true)
    countDownLatch.await()
}

/**
 * Simulates complete playback, i.e. rewinds the playback position to the end and waits its completion.
 */
fun PlayerImpl.simulateCompletePlayback() {
    // As the target position [duration - 100 ms] is taken
    val playbackPosition = max(0, getDuration() - 100)
    seekTo(playbackPosition)
    awaitPlaybackCompletion()
}