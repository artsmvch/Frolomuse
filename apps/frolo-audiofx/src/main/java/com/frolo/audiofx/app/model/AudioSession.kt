package com.frolo.audiofx.app.model

import android.content.pm.ApplicationInfo

data class AudioSession(
    val audioSessionId: Int,
    val applicationInfo: ApplicationInfo
)