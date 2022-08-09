package com.frolo.muse.battery


interface BatteryOptimizationSettings {
    fun isIgnoringBatteryOptimizations(): Boolean
    fun ignoringBatteryOptimizations(): Boolean
}