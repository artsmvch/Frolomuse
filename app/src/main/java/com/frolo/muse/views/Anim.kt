package com.frolo.muse.views

import android.animation.*
import android.view.View


object Anim {

    private open class SimpleAnimatorListener: Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) = Unit

        override fun onAnimationEnd(animation: Animator?) = Unit

        override fun onAnimationCancel(animation: Animator?) = Unit

        override fun onAnimationStart(animation: Animator?) = Unit
    }

    fun fadeIn(view: View, duration: Long = 200, delay: Long = 0) {
        view.animate()
            .alpha(1f)
            .setListener(object : SimpleAnimatorListener() {
                override fun onAnimationEnd(animation: Animator?) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationStart(animation: Animator?) {
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
                override fun onAnimationEnd(animation: Animator?) {
                    view.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animator?) {
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

    fun like(like: View, duration: Long = 200) {
        val anim = ObjectAnimator.ofPropertyValuesHolder(like,
                PropertyValuesHolder.ofFloat("scaleX", 0.65f, 0.88f, 1.0f, 1.12f, 1.20f, 1.25f, 1.20f, 1.12f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 0.5f, 0.85f, 1.0f, 1.12f, 1.20f, 1.25f, 1.20f, 1.12f, 1.0f))
        anim.duration = duration
        anim.start()
    }

    fun unlike(like: View, duration: Long = 200) {
        like(like, duration)
    }

}
