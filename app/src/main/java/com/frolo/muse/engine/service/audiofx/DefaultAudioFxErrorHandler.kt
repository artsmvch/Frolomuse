package com.frolo.muse.engine.service.audiofx

import com.frolo.audiofx.AudioFxImpl
import com.frolo.muse.rx.newSingleThreadScheduler
import com.frolo.muse.rx.subscribeSafely
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable


class DefaultAudioFxErrorHandler : AudioFxImpl.ErrorHandler {

    private val disposables = CompositeDisposable()

    private val workerScheduler: Scheduler by lazy {
        newSingleThreadScheduler("DefaultAudioFxErrorHandler")
    }

    override fun onError(error: Throwable?) {
        error ?: return
        Completable.fromAction { FirebaseCrashlytics.getInstance().recordException(error) }
            .subscribeOn(workerScheduler)
            .subscribeSafely()
            .also { disposables.add(it) }
    }

}