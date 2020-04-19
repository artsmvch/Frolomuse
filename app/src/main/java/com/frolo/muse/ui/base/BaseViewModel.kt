package com.frolo.muse.ui.base

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


abstract class BaseViewModel constructor(
    private val eventLogger: EventLogger
): ViewModel() {

    private val disposables = CompositeDisposable()

    // common error stream
    private val _error: MutableLiveData<Throwable> = SingleLiveEvent()
    val error: LiveData<Throwable> get() = _error

    protected fun logError(err: Throwable) {
        eventLogger.log(err)
        if (ArchTaskExecutor.getInstance().isMainThread) {
            _error.value = err
        } else {
            _error.postValue(err)
        }
    }

    /**
     * Subscribe for (without schedulers)
     */
    fun <T> Flowable<T>.subscribeFor(consumer: (T) -> Unit) {
        this
                .subscribe({ consumer(it) }, { err -> logError(err) })
                .save()
    }

    fun <T> Observable<T>.subscribeFor(consumer: (T) -> Unit) {
        this
                .subscribe({ consumer(it) }, { err -> logError(err) })
                .save()
    }

    fun <T> Single<T>.subscribeFor(consumer: (T) -> Unit) {
        this
                .subscribe({ consumer(it) }, { err -> logError(err) })
                .save()
    }

    fun Completable.subscribeFor(consumer: () -> Unit) {
        this
                .subscribe({ consumer() }, { err -> logError(err) })
                .save()
    }

    /**
     * Subscribe for (with schedulers)
     */
    fun <T> Flowable<T>.subscribeFor(schedulersProvider: SchedulerProvider, consumer: (T) -> Unit) {
        this
                .testable()
                .observeOn(schedulersProvider.main())
                .subscribeOn(schedulersProvider.worker())
                .subscribeFor(consumer)
    }

    fun <T> Single<T>.subscribeFor(schedulersProvider: SchedulerProvider, consumer: (T) -> Unit) {
        this
                .testable()
                .observeOn(schedulersProvider.main())
                .subscribeOn(schedulersProvider.worker())
                .subscribeFor(consumer)
    }

    fun Completable.subscribeFor(schedulersProvider: SchedulerProvider, consumer: () -> Unit) {
        this
                .testable()
                .observeOn(schedulersProvider.main())
                .subscribeOn(schedulersProvider.worker())
                .subscribeFor(consumer)
    }

    /**
     * Testable (additional actions in test mode)
     */
    private fun <T> Single<T>.testable(): Single<T> {
        return this
    }

    private fun <T> Flowable<T>.testable(): Flowable<T> {
        return this
    }

    private fun Completable.testable(): Completable {
        return this
    }

    fun Disposable.save() {
        disposables.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}