package com.frolo.muse.arch

import androidx.lifecycle.Observer


@Deprecated("Use mock() method instead")
inline fun <reified T> StubObserver(): Observer<T> {
    return object : Observer<T> {
        override fun onChanged(t: T) {
            // stub
        }
    }
}