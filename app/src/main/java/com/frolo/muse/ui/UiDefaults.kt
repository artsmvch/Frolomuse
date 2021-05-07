@file:Suppress("FunctionName")

package com.frolo.muse.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.animation.*
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import com.frolo.muse.R
import com.frolo.muse.dp2px


private const val LAYOUT_ANIMATION_DURATION_MEDIUM = 220L

fun ShotItemLayoutAnimation(): Animation {
    val scaleAnimation = ScaleAnimation(0f, 1f, 0f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
        duration = LAYOUT_ANIMATION_DURATION_MEDIUM
        interpolator = OvershootInterpolator(1f)
    }
    val alphaAnimation = AlphaAnimation(0f, 1f).apply {
        duration = LAYOUT_ANIMATION_DURATION_MEDIUM
        interpolator = AccelerateInterpolator(0.5f)
    }
    return AnimationSet(false).apply {
        addAnimation(scaleAnimation)
        addAnimation(alphaAnimation)
    }
}

fun ShotLayoutAnimationController(): LayoutAnimationController {
    return LayoutAnimationController(ShotItemLayoutAnimation()).apply {
        order = LayoutAnimationController.ORDER_NORMAL
        delay = 0.2f
    }
}

fun ProBadgedDrawable(
    context: Context,
    drawable: Drawable,
    @Px topMargin: Int = 2f.dp2px(context).toInt(),
    @Px rightMargin: Int = 2f.dp2px(context).toInt()
): Drawable {
    val badgeDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_pro_badge_16)
    val layers = arrayOf(drawable, badgeDrawable)
    val layerDrawable = LayerDrawable(layers)
    layerDrawable.setLayerGravity(1, Gravity.TOP or Gravity.END)
    layerDrawable.setLayerInsetTop(0, topMargin)
    layerDrawable.setLayerInsetRight(0, rightMargin)
    return layerDrawable
}