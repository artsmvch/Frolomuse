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

fun <T> LiveData<T>.observe(owner: LifecycleOwner, onChanged: ((value: T) -> Unit)) {
    observe(owner, Observer(onChanged))
}

/**
 * Used for cases where T is Void, to make calls cleaner.
 */
fun SingleLiveEvent<Unit>.call() {
    value = Unit
}