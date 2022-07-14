package com.frolo.core.ui.animations

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.frolo.core.ui.R


object AppAnimations {
    private val TAG_KEY_ACTIVE_ANIMATOR = R.id.active_animator

    private fun clearAnimator(view: View) {
        val animator = view.getTag(TAG_KEY_ACTIVE_ANIMATOR) as? Animator
        animator?.cancel()
    }

    private fun saveAnimator(view: View, animator: Animator) {
        view.setTag(TAG_KEY_ACTIVE_ANIMATOR, animator)
    }

    private inline fun animateImpl(target: View, builder: (View) -> Animator) {
        clearAnimator(target)
        val animator = builder.invoke(target)
        animator.start()
        saveAnimator(target, animator)
    }

    fun animateLike(target: View) = animateImpl(target) { view ->
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.4f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.4f, 1f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY)
        animator.duration = 400L
        animator.interpolator = DecelerateInterpolator(2f)
        return@animateImpl animator
    }
}