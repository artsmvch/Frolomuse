@file:Suppress("FunctionName")

package com.frolo.muse.ui

import android.view.animation.*


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