package com.frolo.muse.player.service.audiofx

import android.widget.Toast
import com.frolo.audiofx2.AudioEffect2
import com.frolo.audiofx2.impl.AudioEffect2ErrorHandler
import com.frolo.core.ui.ApplicationWatcher
import com.frolo.muse.BuildInfo
import com.frolo.muse.rx.newSingleThreadScheduler
import com.frolo.muse.rx.subscribeSafely
import com.frolo.ui.ActivityUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable


internal class DefaultAudioEffect2ErrorHandler : AudioEffect2ErrorHandler {

    private val disposables = CompositeDisposable()

    private val workerScheduler: Scheduler by lazy {
        newSingleThreadScheduler("DefaultAudioEffect2ErrorHandler")
    }

    override fun onAudioEffectError(effect: AudioEffect2, error: Throwable) {
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
        ApplicationWatcher.foregroundActivity?.runOnUiThread {
            val activity = ApplicationWatcher.foregroundActivity
            if (activity != null && !ActivityUtils.isFinishingOrDestroyed(activity)) {
                Toast.makeText(activity, "AudioFx2 error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

}