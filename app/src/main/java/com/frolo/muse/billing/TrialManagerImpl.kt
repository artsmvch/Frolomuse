package com.frolo.muse.billing

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.frolo.muse.BuildConfig
import com.frolo.muse.Logger
import com.frolo.muse.firebase.FirebaseRemoteConfigUtil
import com.frolo.rxpreference.RxPreference
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class TrialManagerImpl(
    private val context: Context
) : TrialManager {

    // Disposables
    private val internalDisposables = CompositeDisposable()

    // Schedulers
    private val mainThreadScheduler: Scheduler by lazy { AndroidSchedulers.mainThread() }
    private val queryScheduler: Scheduler by lazy { Schedulers.io() }

    /**
     * SharedPreferences for storing trial information.
     */
    private val trialPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(TRIAL_PREFS_STORAGE_NAME, Context.MODE_PRIVATE)
    }

    override fun sync(): Completable {
        val source = Completable.defer {
            // The trial period is considered valid if it is >= 0L
            val isTrialDurationValid: Boolean = try {
                trialPreferences.getLong(KEY_TRIAL_DURATION_MILLIS, -1L) >= 0L
            } catch (ignored: Throwable) {
                false
            }

            if (!isTrialDurationValid) {
                FirebaseRemoteConfigUtil.fetchAndActivate()
                    .map { config ->
                        // Should throw an exception if the value is empty or invalid
                        config.getValue(REMOTE_KEY_TRIAL_DURATION_MILLIS).asString().toLong()
                    }
                    .doOnSuccess { trialDuration ->
                        trialPreferences.edit { putLong(KEY_TRIAL_DURATION_MILLIS, trialDuration) }
                    }
                    .doOnError { err ->
                        Logger.e(LOG_TAG, "Failed to sync Trial duration millis", err)
                    }
                    .ignoreElement()
            } else {
                Completable.complete()
            }
        }

        return source
            .subscribeOn(queryScheduler)
            .observeOn(mainThreadScheduler)
    }

    override fun getTrialStatus(): Flowable<TrialStatus> {
        return RxPreference.ofLong(trialPreferences, KEY_TRIAL_ACTIVATION_TIME_MILLIS)
            .get()
            .map { activationTimeOptional ->
                val trialDurationMillis: Long = if (DEBUG) {
                    // For debugging, trial duration is only 5 minutes
                    DEBUG_TRIAL_DURATION_MILLIS
                } else {
                    trialPreferences.getLong(KEY_TRIAL_DURATION_MILLIS, DEFAULT_TRIAL_DURATION_MILLIS)
                }

                when {
                    trialDurationMillis <= 0 -> {
                        // The duration is invalid or equal to 0 => no trial available.
                        TrialStatus.NotAvailable
                    }
                    activationTimeOptional.isPresent -> {
                        // There is an activation time present, we need to check if it has expired
                        val currentTimeMillis: Long = getCurrentTimeMillis()
                        val activationTimeMillis: Long = activationTimeOptional.get()
                        val expirationTimeMillis: Long = activationTimeMillis + trialDurationMillis
                        if (currentTimeMillis < expirationTimeMillis) TrialStatus.Activated else TrialStatus.Expired
                    }
                    else -> {
                        // There is no activation time in the preferences.
                        // We consider the trial available (not yet activated).
                        TrialStatus.Available(trialDurationMillis)
                    }
                }
            }
            .observeOn(mainThreadScheduler)
    }

    override fun activateTrialVersion(): Completable {
        val source = Completable.fromAction {
            if (trialPreferences.contains(KEY_TRIAL_ACTIVATION_TIME_MILLIS)) {
                throw IllegalStateException("The trial is already activated")
            }
            val currentTimeMillis: Long = getCurrentTimeMillis()
            trialPreferences.edit { putLong(KEY_TRIAL_ACTIVATION_TIME_MILLIS, currentTimeMillis) }
        }

        return source
            .subscribeOn(queryScheduler)
            .observeOn(mainThreadScheduler)
    }

    override fun resetTrial(): Completable {
        val source = Completable.fromAction {
            trialPreferences.edit { remove(KEY_TRIAL_ACTIVATION_TIME_MILLIS) }
        }

        return source
            .subscribeOn(queryScheduler)
            .observeOn(mainThreadScheduler)
    }

    companion object {
        private val DEBUG = BuildConfig.DEBUG

        private const val LOG_TAG = "TrialManager"

        private const val TRIAL_PREFS_STORAGE_NAME = "com.frolo.muse.billing.Trial"

        private const val REMOTE_KEY_TRIAL_DURATION_MILLIS = "trial_duration_millis"
        private const val KEY_TRIAL_DURATION_MILLIS = "trial_duration_millis"
        private const val KEY_TRIAL_ACTIVATION_TIME_MILLIS = "trial_activation_time_millis"

        private val DEFAULT_TRIAL_DURATION_MILLIS = TimeUnit.DAYS.toMillis(3)
        private val DEBUG_TRIAL_DURATION_MILLIS = TimeUnit.MINUTES.toMillis(5)

        private fun getCurrentTimeMillis(): Long {
            return System.currentTimeMillis()
        }
    }
}