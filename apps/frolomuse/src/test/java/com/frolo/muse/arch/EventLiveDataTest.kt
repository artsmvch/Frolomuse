package com.frolo.muse.arch

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import com.frolo.arch.support.EventLiveData
import com.frolo.muse.thenDoNothing
import com.nhaarman.mockitokotlin2.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class EventLiveDataTest {

    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Test
    fun test_onlyOneObserverNotified() {

        val liveData = EventLiveData<Unit>()

        val owner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        val observer1: Observer<Unit> = mock<Observer<Unit>>().apply {
            whenever(onChanged(eq(Unit))).thenDoNothing()
        }

        val observer2: Observer<Unit> = mock<Observer<Unit>>().apply {
            whenever(onChanged(eq(Unit))).thenDoNothing()
        }

        liveData.observe(owner, observer1)
        liveData.observe(owner, observer2)

        liveData.setValue(Unit)

        verify(observer1, times(1)).onChanged(any())
        verify(observer2, never()).onChanged(any())

    }

    @Test
    fun test_removeObserver() {
        val liveData = EventLiveData<Unit>()

        val owner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        val observer: Observer<Unit> = mock<Observer<Unit>>().apply {
            whenever(onChanged(eq(Unit))).thenDoNothing()
        }

        liveData.observe(owner, observer)

        owner.currentState = Lifecycle.State.DESTROYED

        liveData.setValue(Unit)

        verify(observer, never()).onChanged(any())
    }

}