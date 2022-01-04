package com.frolo.muse.rx

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import com.frolo.muse.arch.TestLifecycleOwner
import io.reactivex.Observable
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit


@RunWith(JUnit4::class)
class RxArchTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun test_DisposeOnPauseOf() {
        val lifecycleOwner = TestLifecycleOwner()
        val observable = Observable.interval(1, TimeUnit.SECONDS)
        val disposable = observable.subscribe().disposeOnPauseOf(lifecycleOwner)

        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.RESUMED
        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.STARTED
        assertTrue(disposable.isDisposed)
    }

    @Test
    fun test_DisposeOnStopOf() {
        val lifecycleOwner = TestLifecycleOwner()
        val observable = Observable.interval(1, TimeUnit.SECONDS)
        val disposable = observable.subscribe().disposeOnStopOf(lifecycleOwner)

        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.RESUMED
        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.STARTED
        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.CREATED
        assertTrue(disposable.isDisposed)
    }

    @Test
    fun test_DisposeOnDestroyOf() {
        val lifecycleOwner = TestLifecycleOwner()
        val observable = Observable.interval(1, TimeUnit.SECONDS)
        val disposable = observable.subscribe().disposeOnDestroyOf(lifecycleOwner)

        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.RESUMED
        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.STARTED
        assertFalse(disposable.isDisposed)

        lifecycleOwner.currentState = Lifecycle.State.DESTROYED
        assertTrue(disposable.isDisposed)
    }

}