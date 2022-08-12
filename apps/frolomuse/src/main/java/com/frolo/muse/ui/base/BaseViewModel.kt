package com.frolo.muse.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.frolo.arch.support.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.ConcurrentHashMap


abstract class BaseViewModel constructor(
    private val eventLogger: EventLogger
): ViewModel() {

    // Container for any disposables that need to be disposed when the view model is cleared
    private val disposables = CompositeDisposable()

    // Container for keyed disposables that need to be disposed when the view model is cleared
    private val keyedDisposables: MutableMap<String, Disposable> = ConcurrentHashMap<String, Disposable>()

    // Common error stream
    private val _error = SingleLiveEvent<Throwable>()
    @Deprecated("Create a separate error stream for your needs")
    val error: LiveData<Throwable> get() = _error

    protected fun logError(err: Throwable) {
        eventLogger.log(err)
        _error.postValue(err)
    }

    /**
     * Subscribes to [this] Flowable source and saves the result disposable for cleanup when the view model is cleared.
     * [onNext] will be called every time the source emits a new element.
     * The result disposable can be associated with the given [key].
     * If the [key] is not null, then any previous disposable associated with it will be disposed.
     * This may be useful when only one unit of a particular task can be performed at a time.
     */
    fun <T> Flowable<T>.subscribeFor(key: String? = null, onNext: (T) -> Unit) {
        this
            .subscribe(onNext, ::logError)
            .save(key = key)
    }

    /**
     * Subscribes to [this] Observable source and saves the result disposable for cleanup when the view model is cleared.
     * [onNext] will be called every time the source emits a new element.
     * The result disposable can be associated with the given [key].
     * If the [key] is not null, then any previous disposable associated with it will be disposed.
     * This may be useful when only one unit of a particular task can be performed at a time.
     */
    protected fun <T> Observable<T>.subscribeFor(key: String? = null, onNext: (T) -> Unit) {
        this
            .subscribe(onNext, ::logError)
            .save(key = key)
    }

    /**
     * Subscribes to [this] Single source and saves the result disposable for cleanup when the view model is cleared.
     * [onSuccess] will be called when the source completes successfully with a result.
     * The result disposable can be associated with the given [key].
     * If the [key] is not null, then any previous disposable associated with it will be disposed.
     * This may be useful when only one unit of a particular task can be performed at a time.
     */
    protected fun <T> Single<T>.subscribeFor(key: String? = null, onSuccess: (T) -> Unit) {
        this
            .subscribe(onSuccess, ::logError)
            .save(key = key)
    }

    /**
     * Subscribes to [this] Completable source and saves the result disposable for cleanup when the view model is cleared.
     * [onComplete] will be called when the source completes successfully.
     * The result disposable can be associated with the given [key].
     * If the [key] is not null, then any previous disposable associated with it will be disposed.
     * This may be useful when only one unit of a particular task can be performed at a time.
     */
    protected fun Completable.subscribeFor(key: String? = null, onComplete: () -> Unit) {
        this
            .subscribe(onComplete, ::logError)
            .save(key = key)
    }

    /**
     * Saves [this] disposable to be disposed when the view model is cleared.
     * A disposable can be associated with [key],
     * so calling this method again with the same key will dispose the previous disposable.
     * This may be useful when only one unit of a particular task can be performed at a time.
     */
    protected fun Disposable.save(key: String? = null) {
        disposables.add(this)

        // If the key is not null, then this disposable gets associated with the key,
        // and the old one is disposed, if any.
        if (key != null) {
            keyedDisposables.put(key, this)?.dispose()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}