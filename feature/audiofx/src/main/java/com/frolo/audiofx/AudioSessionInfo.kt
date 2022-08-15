package com.frolo.audiofx

import android.graphics.drawable.Drawable

interface AudioSessionInfo {
    val name: CharSequence
    val description: CharSequence?
    val icon: Drawable?
}