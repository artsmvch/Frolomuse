package com.frolo.muse.model.ads


data class AdMobBannerConfig(
    val isEnabled: Boolean,
    val unitId: String,
    val minAppVersionCode: Long,
    val minFirstInstallTime: Long,
    val minLaunchCount: Int
)