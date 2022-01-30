package com.frolo.muse.ui.base

import androidx.lifecycle.LiveData
import com.frolo.muse.arch.SingleLiveEvent


object RESPermissionBus {
    @Suppress("ObjectPropertyName")
    private val _dispatcher = SingleLiveEvent<Unit>()
    val dispatcher: LiveData<Unit> get() = _dispatcher

    fun dispatch() {
        _dispatcher.value = Unit
    }
}