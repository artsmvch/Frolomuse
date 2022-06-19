package com.frolo.arch.support

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.atomic.AtomicBoolean


class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val observers = CopyOnWriteArraySet<ObserverWrapper<in T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observe(owner, wrapper)
    }

    override fun removeObservers(owner: LifecycleOwner) {
        observers.clear()
        super.removeObservers(owner)
    }

    override fun removeObserver(observer: Observer<in T>) {
        val wrapper = ObserverWrapper(observer)
        observers.remove(wrapper)
        super.removeObserver(wrapper)
    }

    @MainThread
    override fun setValue(t: T?) {
        observers.forEach { it.onSetValue() }
        super.setValue(t)
    }

    private class ObserverWrapper<T>(observer: Observer<T>) : AbsObserverWrapper<T>(observer) {
        private val isPending = AtomicBoolean(false)

        override fun onChanged(value: T?) {
            if (isPending.compareAndSet(true, false)) {
                wrapped.onChanged(value)
            }
        }

        fun onSetValue() {
            isPending.set(true)
        }
    }
}