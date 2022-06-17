package com.frolo.muse.arch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.*
import io.reactivex.disposables.Disposable


private val EMPTY_CONSUMER: (Any) -> Unit = { }

/**
 * Subscribes to the [source] when this live data becomes active,
 * meaning it has at least one active observer;
 * And disposes the subscription if the live data becomes inactive.
 * All emitted elements are published to the live data.
 */
fun <T> createLiveData(
    source: Flowable<T>,
    onError: ((Throwable) -> Unit) = EMPTY_CONSUMER
): LiveData<T> {
    return object : MutableLiveData<T>() {
        var disposable: Disposable? = null
        var completed: Boolean = false

        override fun onActive() {
            maybeSubscribe()
        }

        fun maybeSubscribe() {
            if (completed) {
                // The publisher has completed the emission earlier,
                // all elements have been sent to the live data
                // so we do not need to subscribe again.
                return
            }
            disposable?.dispose()
            disposable = source
                .subscribe(
                    { value ->
                        // We don't know on which scheduler the source is being
                        // observed, so we post the value to the main thread.
                        postValue(value)
                    },
                    onError,
                    {
                        completed = true
                        disposable = null
                    }
                )
        }

        override fun onInactive() {
            disposable?.dispose()
            disposable = null
        }
    }
}

fun <T> createLiveData(
    source: Observable<T>,
    onError: ((Throwable) -> Unit) = EMPTY_CONSUMER
): LiveData<T> = createLiveData(source.toFlowable(BackpressureStrategy.ERROR), onError)

fun <T> createLiveData(
    source: Single<T>,
    onError: ((Throwable) -> Unit) = EMPTY_CONSUMER
): LiveData<T> = createLiveData(source.toFlowable(), onError)

fun <T> createLiveData(
    source: Maybe<T>,
    onError: ((Throwable) -> Unit) = EMPTY_CONSUMER
): LiveData<T> = createLiveData(source.toFlowable(), onError)

fun <T> createLiveData(
    source: Completable,
    onError: ((Throwable) -> Unit) = EMPTY_CONSUMER
): LiveData<T> = createLiveData(source.toFlowable(), onError)