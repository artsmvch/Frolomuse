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

/**
 * Used for cases where T is Void, to make calls cleaner.
 */
fun EventLiveData<Unit>.call() {
    setValue(Unit)
}

fun <T> liveDataOf(item: T?) = object : LiveData<T>(item) { }

/**
 * This function does the same as [androidx.lifecycle.Transformations],
 * except that it allows to set [initialValue] for the returned live data.
 */
fun <X: Any, Y: Any> LiveData<X>.map(initialValue: Y, mapFunction: (x: X?) -> Y?): LiveData<Y> {
    val result = MediatorLiveData<Y>()
    result.value = initialValue
    result.addSource(this) { x -> result.setValue(mapFunction.invoke(x)) }
    return result
}

fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> = Transformations.distinctUntilChanged(this)

fun <T, R> combineMultiple(vararg liveData: LiveData<T>, combiner: (values: List<T?>) -> R?): LiveData<R> {
    val mediator = MediatorLiveData<R>()
    liveData.forEach { source ->
        mediator.addSource(source) { sourceValue ->
            val values = List<T?>(liveData.size) { index ->
                val targetLiveData = liveData[index]
                if (targetLiveData == source) {
                    sourceValue
                } else {
                    targetLiveData.value
                }
            }
            mediator.value = combiner.invoke(values)
        }
    }
    return mediator
}