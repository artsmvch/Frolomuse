package com.frolo.muse.arch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry


class TestLifecycleOwner constructor(
    initialState: Lifecycle.State = Lifecycle.State.INITIALIZED
): LifecycleOwner {

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    init {
        lifecycleRegistry.currentState = initialState
    }

    var currentState: Lifecycle.State
        get() = lifecycleRegistry.currentState
        set(value) {
            lifecycleRegistry.currentState = value
        }

    override val lifecycle: Lifecycle get() = lifecycleRegistry

}