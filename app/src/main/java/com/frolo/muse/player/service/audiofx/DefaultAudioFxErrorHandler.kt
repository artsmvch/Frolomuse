package com.frolo.muse.player.service.audiofx

import android.widget.Toast
import com.frolo.audiofx.AudioFxImpl
import com.frolo.core.ui.ActivityWatcher
import com.frolo.muse.BuildInfo
import com.frolo.muse.rx.newSingleThreadScheduler
import com.frolo.muse.rx.subscribeSafely
import com.frolo.ui.ActivityUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable


class DefaultAudioFxErrorHandler(
    private val activityWatcher: ActivityWatcher
) : AudioFxImpl.ErrorHandler {

    private val disposables = CompositeDisposable()

    private val workerScheduler: Scheduler by lazy {
        newSingleThreadScheduler("DefaultAudioFxErrorHandler")
    }

    override fun onError(error: Throwable?) {
        error ?: return
        dispatchErrorToFirebaseAsync(error)
        toastError(error)
    }

    private fun dispatchErrorToFirebaseAsync(error: Throwable) {
        Completable.fromAction { FirebaseCrashlytics.getInstance().recordException(error) }
            .subscribeOn(workerScheduler)
            .subscribeSafely()
            .also { disposables.add(it) }
    }

    private fun toastError(error: Throwable) {
        if (!BuildInfo.isDebug()) {
            return
        }
        activityWatcher.foregroundActivity?.runOnUiThread {
            val activity = activityWatcher.foregroundActivity
            if (activity != null && !ActivityUtils.isFinishingOrDestroyed(activity)) {
                Toast.makeText(activity, "AudioFx error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

}