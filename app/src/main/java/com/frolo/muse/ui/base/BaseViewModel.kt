package com.frolo.muse.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.reactivestreams.Subscription
import java.util.concurrent.ConcurrentHashMap


abstract class BaseViewModel constructor(
    private val eventLogger: EventLogger
): ViewModel() {

    // Container for any disposables that need to be disposed when the view model is cleared
    private val disposables = CompositeDisposable()

    // Container for keyed disposables that need to be disposed when the view model is cleared
    private val keyedDisposables: MutableMap<String, Disposable> = ConcurrentHashMap<String, Disposable>()
    // Container for keyed subscription that need to be cancelled when the view model is cleared
    private val keyedSubscriptions: MutableMap<String, Subscription> = ConcurrentHashMap<String, Subscription>()

    // Common error stream
    private val _error = SingleLiveEvent<Throwable>()
    val error: LiveData<Throwable> get() = _error

    protected fun logError(err: Throwable) {
        eventLogger.log(err)
        _error.postValue(err)
    }

    /**
     * Subscribes to [this] Flowable source and saves the result subscription for cleanup when the view model is cleared.
     * [onNext] will be called every time the source emits a new element.
     * The result subscription can be associated with the given [key].
     * If the [key] is not null, then any previous subscription associated with it will be cancelled.
     * This may be useful when only one unit of a particular task can be performed at a time.
     */
    fun <T> Flowable<T>.subscribeFor(key: String? = null, onNext: (T) -> Unit) {
        this
            .doOnSubscribe { subscription ->
                if (key != null) {
                    keyedSubscriptions.put(key, subscription)?.cancel()
                }
            }
            .subscribe(onNext, ::logError)
            .save()
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
            .doOnSubscribe { disposable ->
                if (key != null) {
                    keyedDisposables.put(key, disposable)?.dispose()
                }
            }
            .subscribe(onNext, ::logError)
            .save()
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
            .doOnSubscribe { disposable ->
                if (key != null) {
                    keyedDisposables.put(key, disposable)?.dispose()
                }
            }
            .subscribe(onSuccess, ::logError)
            .save()
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
            .doOnSubscribe { disposable ->
                if (key != null) {
                    keyedDisposables.put(key, disposable)?.dispose()
                }
            }
            .subscribe(onComplete, ::logError)
            .save()
    }

    /**
     * Saves [this] disposable to be disposed when the view model is cleared.
     */
    protected fun Disposable.save() {
        disposables.add(this)
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
    }

}