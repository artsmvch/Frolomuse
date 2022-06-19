package com.frolo.core.ui.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder


private const val DEFAULT_SQUIRCLE_CURVATURE = 3.2

fun RequestBuilder<Drawable>.squircleCrop(curvature: Double = DEFAULT_SQUIRCLE_CURVATURE): RequestBuilder<Drawable> {
    return transform(SquircleTransformation(curvature))
}