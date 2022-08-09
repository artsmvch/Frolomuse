package com.frolo.muse.battery

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import com.frolo.core.ui.ApplicationWatcher
import com.frolo.core.ui.application.ApplicationForegroundStatusRegistry
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposables

class BatteryOptimizationSettingsImpl(
    private val application: Application
): BatteryOptimizationSettings {

    private fun isAppInForeground(): Flowable<Boolean> {
        return Flowable.create(
            { emitter ->
                val foregroundStatusObserver = ApplicationForegroundStatusRegistry.Observer { isInForeground ->
                    emitter.onNext(isInForeground)
                }
                val foregroundStatusRegistry = ApplicationWatcher.applicationForegroundStatusRegistry
                foregroundStatusRegistry.addObserver(foregroundStatusObserver)
                emitter.setDisposable(
                    Disposables.fromAction {
                        foregroundStatusRegistry.removeObserver(foregroundStatusObserver)
                    }
                )
                emitter.onNext(foregroundStatusRegistry.isInForeground)
            },
            BackpressureStrategy.LATEST
        )
    }

    override fun isIgnoringBatteryOptimizations(): Flowable<Boolean> {
        return isAppInForeground()
            .observeOn(AndroidSchedulers.mainThread())
            // We are only interested in the fact that the user returns to the application
            .filter { isInForeground -> isInForeground }
            .map {
                val powerManager = application.getSystemService(Context.POWER_SERVICE)
                    as? PowerManager
                    ?: return@map true
                return@map powerManager.isIgnoringBatteryOptimizations(application.packageName)
            }
    }

    override fun ignoringBatteryOptimizations(): Completable {
        val source = Completable.fromAction {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            application.startActivity(intent)
        }
        return source.observeOn(AndroidSchedulers.mainThread())
    }
}