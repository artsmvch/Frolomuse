package com.frolo.muse.views

import android.animation.*
import android.view.View
import android.view.animation.OvershootInterpolator


@Deprecated("Create your own view animation")
object Anim {
    private open class SimpleAnimatorListener: Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) = Unit
        override fun onAnimationEnd(animation: Animator) = Unit
        override fun onAnimationCancel(animation: Animator) = Unit
        override fun onAnimationStart(animation: Animator) = Unit
    }

    fun fadeIn(view: View, duration: Long = 200, delay: Long = 0) {
        view.animate()
            .alpha(1f)
            .setListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationStart(animation: Animator) {
                    view.visibility = View.VISIBLE
                }
            })
            .setDuration(duration)
            .setStartDelay(delay)
            .start()
    }

    fun fadeOut(view: View, duration: Long = 200, delay: Long = 0) {
        view.animate()
            .alpha(0f)
            .setListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animator) {
                    // Do NOT set the view's visibility to VISIBLE when the animation starts.
                    // Otherwise, if the view's visibility was invisible(gone) then for the user,
                    // the view appears for the specified duration.
                    // But in fact, the view should not appear only to show the animation.
                    // If the view is invisible(gone) to the user then it should remain invisible(gone) to the user.
                }
            })
            .setDuration(duration)
            .setStartDelay(delay)
            .start()
    }

    fun like(view: View, duration: Long = 180L) {
        val scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.65f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.65f, 1f)
        val animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY)
        animator.duration = duration
        animator.interpolator = OvershootInterpolator(8f)
        animator.start()
    }

    fun unlike(view: View, duration: Long = 180L) {
        like(view, duration)
    }

}
