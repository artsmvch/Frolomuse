package com.frolo.arch.support

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


/**
 * A LiveData implementation that dispatches data to only one observer.
 * Typically, this will be the first observer to register using the [observe]
 * and [observeForever] methods.
 */
class EventLiveData <T> : LiveData<T>() {

    private var isConsumed: Boolean = true

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, ObserverWrapper(observer))
    }

    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(ObserverWrapper(observer))
    }

    public override fun setValue(value: T) {
        isConsumed = false
        super.setValue(value)
    }

    private inner class ObserverWrapper<T>(observer: Observer<T>) : AbsObserverWrapper<T>(observer) {

        override fun onChanged(value: T?) {
            if (!isConsumed) {
                wrapped.onChanged(value)
                isConsumed = true
            }
        }

    }
}