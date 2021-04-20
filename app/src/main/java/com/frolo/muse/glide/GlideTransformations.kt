package com.frolo.muse.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder


fun RequestBuilder<Drawable>.squircleCrop(
    curvature: Double = SquircleTransformation.DEFAULT_CURVATURE
): RequestBuilder<Drawable> {
    return transform(SquircleTransformation(curvature))
}