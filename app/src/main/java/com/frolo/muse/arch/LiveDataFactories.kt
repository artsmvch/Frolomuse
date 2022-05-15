package com.frolo.muse.arch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.*
import io.reactivex.disposables.Disposable


private val EMPTY_CONSUMER: (Any) -> Unit = { }

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
                return
            }
            disposable?.dispose()
            disposable = source
                .subscribe(
                    { value ->
                        postValue(value)
                    },
                    { error ->
                        onError.invoke(error)
                    },
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