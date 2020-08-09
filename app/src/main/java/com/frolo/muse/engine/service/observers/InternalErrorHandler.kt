package com.frolo.muse.engine.service.observers

import android.content.Context
import android.util.Log
import com.frolo.muse.BuildConfig
import com.frolo.muse.engine.Player
import com.frolo.muse.engine.SimplePlayerObserver
import com.google.firebase.crashlytics.FirebaseCrashlytics


/**
 * Handles internal errors occurred in [Player].
 * The implementation reports all exceptions to Firebase.
 * If it's in debug mode, it also logs the errors via [Log].
 */
class InternalErrorHandler constructor(
    private val context: Context
): SimplePlayerObserver() {

    override fun onInternalErrorOccurred(player: Player, error: Throwable) {
        if (DEBUG) Log.e(LOG_TAG, "An internal error occurred", error)
        FirebaseCrashlytics.getInstance().recordException(error);
    }

    companion object {

        private val DEBUG = BuildConfig.DEBUG
        private const val LOG_TAG = "PlayerErrorHandler"

    }

}
