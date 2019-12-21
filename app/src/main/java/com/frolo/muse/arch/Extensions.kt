package com.frolo.muse.arch

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

fun <T,R,U> combine(first: LiveData<R>, second: LiveData<U>, combiner: (R?, U?) -> T): LiveData<T> {
    return MediatorLiveData<T>().apply {
        addSource(first) { v: R ->
            this.value = combiner(v, second.value)
        }
        addSource(second) { v: U ->
            this.value = combiner(first.value, v)
        }
    }
}

/**
 * Convenience method for observing live data.
 */
fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChanged: ((value: T?) -> Unit)) {
    observe(owner, Observer(onChanged))
}

/**
 * Same as [observe] but only calls [onChanged] if the value is not null.
 */
fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, onChanged: ((value: T) -> Unit)) {
    observe(owner) {
        if (it != null) onChanged.invoke(it)
    }
}

/**
 * Used for cases where T is Void, to make calls cleaner.
 */
fun SingleLiveEvent<Unit>.call() {
    value = Unit
}

fun <T> liveDataOf(item: T?) = object : LiveData<T>(item) { }