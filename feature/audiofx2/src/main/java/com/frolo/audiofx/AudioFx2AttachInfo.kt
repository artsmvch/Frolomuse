package com.frolo.audiofx

import android.graphics.drawable.Drawable

interface AudioFx2AttachInfo {
    val name: CharSequence
    val description: CharSequence?
    val icon: Drawable?
}