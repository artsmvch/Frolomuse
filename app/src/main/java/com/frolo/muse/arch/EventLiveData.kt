package com.frolo.muse.arch

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


/**
 * A LiveData implementation that dispatches data to only one observer.
 * Typically, this will be the first observer to register using the [observe] and [observeForever] methods.
 * TODO: check if observers are removed properly here
 */
class EventLiveData <T> : LiveData<T>() {

    private var consumed: Boolean = true

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, ObserverWrapper(observer))
    }

    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(ObserverWrapper(observer))
    }

    public override fun setValue(value: T) {
        consumed = false
        super.setValue(value)
    }

    private inner class ObserverWrapper<T> constructor(val observer: Observer<T>) : Observer<T> {

        override fun onChanged(t: T?) {
            if (!consumed) {
                observer.onChanged(t)
                consumed = true
            }
        }

    }
}