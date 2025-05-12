package com.frolo.muse.firebase

import android.annotation.SuppressLint
import androidx.annotation.GuardedBy
import com.frolo.muse.BuildConfig
import com.frolo.logger.api.Logger
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.google.firebase.remoteconfig.ktx.get
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.processors.FlowableProcessor
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


object FirebaseRemoteConfigCache {

    private const val LOG_TAG = "FirebaseRemoteConfigCache"

    private const val EMPTY_STRING_VALUE = ""

    private val MIN_FETCH_INTERVAL_IN_SECONDS = TimeUnit.HOURS.toSeconds(12L)

    private val internalDisposables = CompositeDisposable()

    private val valueProcessorsLock = Any()

    @get:GuardedBy("valueProcessorsLock")
    private val valueProcessors by lazy { HashMap<String, FlowableProcessor<String>>() }

    @SuppressLint("CheckResult")
    private fun obtainValueProcessor(key: String): FlowableProcessor<String> {
        return synchronized(valueProcessorsLock) {
            var processor = valueProcessors[key]
            if (processor == null) {
                val newProcessor = BehaviorProcessor.create<String>().toSerialized()
                valueProcessors[key] = newProcessor
                getSourceImpl(key, true, MIN_FETCH_INTERVAL_IN_SECONDS)
                    .timeout(10L, TimeUnit.SECONDS)
                    .doOnSubscribe { disposable -> internalDisposables.add(disposable) }
                    .map { value -> value.asString() }
                    .doOnError { error ->
                        val wrappedError = FirebaseException(error)
                        Logger.e(LOG_TAG, wrappedError)
                    }
                    .onErrorReturnItem(EMPTY_STRING_VALUE)
                    .subscribe { value -> newProcessor.onNext(value) }
                processor = newProcessor
            }
            return@synchronized processor
        }
    }

    private fun getSourceImpl(
        key: String,
        fetch: Boolean,
        minimumFetchIntervalInSeconds: Long? = null
    ): Single<FirebaseRemoteConfigValue> {
        if (!BuildConfig.GOOGLE_SERVICES_ENABLED) {
            return Single.error(UnsupportedOperationException("Firebase is not enabled"))
        }
        val configSource = if (fetch) {
            FirebaseRemoteConfigUtil.fetchAndActivate(minimumFetchIntervalInSeconds)
        } else {
            FirebaseRemoteConfigUtil.getActivatedConfig()
        }
        return configSource.map { config -> config[key] }
    }

    fun clear() {
        synchronized(valueProcessorsLock) {
            valueProcessors.clear()
            internalDisposables.clear()
        }
    }

    fun getString(key: String): Single<String> {
        return Single.defer { obtainValueProcessor(key).firstOrError() }
            .subscribeOn(Schedulers.computation())
            .onErrorReturnItem(EMPTY_STRING_VALUE)
    }

    fun getBool(key: String): Single<Boolean> {
        return getString(key).map { stringValue ->
            stringValue == "true" || stringValue == "yes" || stringValue == "y"
        }
    }
}