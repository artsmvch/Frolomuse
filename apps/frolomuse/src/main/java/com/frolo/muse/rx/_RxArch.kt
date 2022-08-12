package com.frolo.muse.rx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable


internal class OnLifecycleEventObserver(
    private val events: Set<Lifecycle.Event>,
    private val callback: () -> Unit
): LifecycleObserver {

    constructor(event: Lifecycle.Event, callback: () -> Unit): this(setOf(event), callback)

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        if (events.contains(Lifecycle.Event.ON_CREATE)) callback.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (events.contains(Lifecycle.Event.ON_START)) callback.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (events.contains(Lifecycle.Event.ON_RESUME)) callback.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (events.contains(Lifecycle.Event.ON_PAUSE)) callback.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        if (events.contains(Lifecycle.Event.ON_STOP)) callback.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        if (events.contains(Lifecycle.Event.ON_DESTROY)) callback.invoke()
    }
}

fun Disposable.disposeOnPauseOf(lifecycle: Lifecycle): Disposable {
    val observer = OnLifecycleEventObserver(Lifecycle.Event.ON_PAUSE) { dispose() }
    lifecycle.addObserver(observer)
    return this
}

fun Disposable.disposeOnPauseOf(lifecycleOwner: LifecycleOwner): Disposable {
    return disposeOnPauseOf(lifecycleOwner.lifecycle)
}

fun Disposable.disposeOnStopOf(lifecycle: Lifecycle): Disposable {
    val observer = OnLifecycleEventObserver(Lifecycle.Event.ON_STOP) { dispose() }
    lifecycle.addObserver(observer)
    return this
}

fun Disposable.disposeOnStopOf(lifecycleOwner: LifecycleOwner): Disposable {
    return disposeOnStopOf(lifecycleOwner.lifecycle)
}

fun Disposable.disposeOnDestroyOf(lifecycle: Lifecycle): Disposable {
    val observer = OnLifecycleEventObserver(Lifecycle.Event.ON_DESTROY) { dispose() }
    lifecycle.addObserver(observer)
    return this
}

fun Disposable.disposeOnDestroyOf(lifecycleOwner: LifecycleOwner): Disposable {
    return disposeOnDestroyOf(lifecycleOwner.lifecycle)
}