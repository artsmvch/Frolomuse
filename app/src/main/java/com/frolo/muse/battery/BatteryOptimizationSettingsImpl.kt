package com.frolo.muse.battery

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import javax.inject.Inject

class BatteryOptimizationSettingsImpl @Inject constructor(
    private val application: Application
): BatteryOptimizationSettings {
    override fun isIgnoringBatteryOptimizations(): Boolean {
        // Observe app foreground status
        val powerManager = application.getSystemService(Context.POWER_SERVICE)
            as? PowerManager
            ?: return true
        return powerManager.isIgnoringBatteryOptimizations(application.packageName)
    }

    override fun ignoringBatteryOptimizations(): Boolean {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        return try {
            application.startActivity(intent)
            true
        } catch (ignored: Throwable) {
            false
        }
    }
}