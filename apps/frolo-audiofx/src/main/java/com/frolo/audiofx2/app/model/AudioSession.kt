package com.frolo.audiofx2.app.model

import android.content.pm.ApplicationInfo

data class AudioSession(
    val audioSessionId: Int,
    val applicationInfo: ApplicationInfo
)