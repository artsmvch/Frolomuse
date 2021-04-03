package com.frolo.muse.ui.base

import android.app.Application
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frolo.muse.arch.SingleLiveEvent
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


abstract class BaseAndroidViewModel constructor(
        application: Application,
        private val eventLogger: EventLogger
) : AndroidViewModel(application) {

    private val disposables = CompositeDisposable()

    private val _error: MutableLiveData<Throwable> = SingleLiveEvent()
    val error: LiveData<Throwable> = _error

    protected val justApplication: Application get() = getApplication()

    private fun logError(err: Throwable) {
        eventLogger.log(err)
        if (ArchTaskExecutor.getInstance().isMainThread) {
            _error.value = err
        } else {
            _error.postValue(err)
        }
    }

    fun <T> Flowable<T>.subscribeFor(consumer: (T) -> Unit) {
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

    fun <T> Flowable<T>.subscribeFor(schedulerProvider: SchedulerProvider, consumer: (T) -> Unit) {
        this
                .observeOn(schedulerProvider.main())
                .subscribeOn(schedulerProvider.worker())
                .subscribe({ consumer(it) }, { err -> logError(err) })
                .save()
    }

    fun <T> Single<T>.subscribeFor(schedulerProvider: SchedulerProvider, consumer: (T) -> Unit) {
        this
                .observeOn(schedulerProvider.main())
                .subscribeOn(schedulerProvider.worker())
                .subscribe({ consumer(it) }, { err -> logError(err) })
                .save()
    }

    fun Completable.subscribeFor(schedulerProvider: SchedulerProvider, consumer: () -> Unit) {
        this
                .observeOn(schedulerProvider.main())
                .subscribeOn(schedulerProvider.worker())
                .subscribe({ consumer() }, { err -> logError(err) })
                .save()
    }

    fun Disposable.save() {
        disposables.add(this)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}