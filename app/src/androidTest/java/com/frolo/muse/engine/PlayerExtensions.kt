package com.frolo.muse.engine

import java.util.concurrent.CountDownLatch


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