package com.frolo.arch.support

import androidx.lifecycle.*

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
 * Same as [LiveData.observe] but only calls [onChanged] if the value is not null.
 */
fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, onChanged: ((value: T & Any) -> Unit)) {
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

/**
 * Used for cases where T is Void, to make calls cleaner.
 */
fun EventLiveData<Unit>.call() {
    setValue(Unit)
}

fun <T: Any> liveDataOf(item: T): LiveData<T> = MutableLiveData(item)

/**
 * Same as the official [androidx.lifecycle.map], except that the returned live data is
 * seeded with [initialValue] so observers get a value immediately, before this source
 * has emitted anything.
 */
fun <X, Y: Any> LiveData<X>.mapWithInitial(initialValue: Y, mapFunction: (x: X?) -> Y?): LiveData<Y> {
    val transformed = map(mapFunction)
    return MediatorLiveData<Y>().apply {
        value = initialValue
        addSource(transformed) { value = it }
    }
}