package com.frolo.muse.arch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry


class TestLifecycleOwner constructor(
    initialState: Lifecycle.State = Lifecycle.State.INITIALIZED
): LifecycleOwner {

    private val lifecycle by lazy { LifecycleRegistry(this) }

    init {
        lifecycle.currentState = initialState
    }

    var currentState: Lifecycle.State
        get() = lifecycle.currentState
        set(value) {
            lifecycle.currentState = value
        }

    override fun getLifecycle(): Lifecycle = lifecycle

}