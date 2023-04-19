package com.frolo.muse.model.ads

data class FacebookBannerConfig(
    val isEnabled: Boolean,
    val placementId: String,
    val minAppVersionCode: Long,
    val minFirstInstallTime: Long,
    val minLaunchCount: Int
)