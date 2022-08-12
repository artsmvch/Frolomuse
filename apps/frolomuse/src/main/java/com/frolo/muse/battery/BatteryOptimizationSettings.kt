package com.frolo.muse.battery

import io.reactivex.Completable
import io.reactivex.Flowable


interface BatteryOptimizationSettings {
    fun isIgnoringBatteryOptimizations(): Flowable<Boolean>
    fun ignoringBatteryOptimizations(): Completable
}