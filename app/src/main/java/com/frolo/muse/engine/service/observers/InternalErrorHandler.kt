package com.frolo.muse.engine.service.observers

import android.content.Context
import android.util.Log
import com.frolo.muse.BuildConfig
import com.frolo.player.Player
import com.frolo.player.SimplePlayerObserver
import com.frolo.muse.rx.newSingleThreadScheduler
import com.frolo.muse.rx.subscribeSafely
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable


/**
 * Handles internal errors occurred in [Player].
 * The implementation reports all exceptions to Firebase.
 * If it's in debug mode, it also logs the errors via [Log].
 */
class InternalErrorHandler constructor(
    private val context: Context
): SimplePlayerObserver() {

    private val disposables = CompositeDisposable()

    private val workerScheduler: Scheduler by lazy {
        newSingleThreadScheduler("PlayerInternalErrorHandler")
    }

    override fun onInternalErrorOccurred(player: Player, error: Throwable) {
        if (DEBUG) Log.e(LOG_TAG, "An internal error occurred", error)
        Completable.fromAction { FirebaseCrashlytics.getInstance().recordException(error) }
            .subscribeOn(workerScheduler)
            .subscribeSafely()
            .also { disposables.add(it) }
    }

    override fun onShutdown(player: Player) {
        disposables.dispose()
    }

    companion object {

        private val DEBUG = BuildConfig.DEBUG
        private const val LOG_TAG = "PlayerErrorHandler"

    }

}
