package com.frolo.muse.views


import android.animation.*
import android.view.View
import android.view.animation.Animation

object Anim {
    open class SimpleAnimationListener: Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) = Unit
        override fun onAnimationEnd(animation: Animation) = Unit
        override fun onAnimationRepeat(animation: Animation) = Unit
    }

    fun paintBackground(fromColor: Int, toColor: Int, duration: Long = 200, delay: Long = 0): ValueAnimator {
        return ValueAnimator().also { anim ->
            anim.setIntValues(fromColor, toColor)
            anim.setEvaluator(ArgbEvaluator())
            anim.duration = duration
            anim.startDelay = delay
        }
    }

    fun fadeIn(view: View, duration: Long = 200, delay: Long = 0) {
        view.animate()
                .alpha(1f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) = Unit
                    override fun onAnimationEnd(animation: Animator?) {
                        view.visibility = View.VISIBLE
                    }
                    override fun onAnimationCancel(animation: Animator?) = Unit
                    override fun onAnimationStart(animation: Animator?) {
                        view.visibility = View.VISIBLE
                    }})
                .setDuration(duration)
                .setStartDelay(delay)
                .start()
    }

    fun fadeOut(view: View, duration: Long = 200, delay: Long = 0) {
        view.animate()
                .alpha(0f)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) = Unit
                    override fun onAnimationEnd(animation: Animator?) {
                        view.visibility = View.INVISIBLE
                    }
                    override fun onAnimationCancel(animation: Animator?) = Unit
                    override fun onAnimationStart(animation: Animator?) {
                        view.visibility = View.VISIBLE
                    }})
                .setDuration(duration)
                .setStartDelay(delay)
                .start()
    }

    fun alpha(view: View, toAlpha: Float, duration: Long = 200, delay: Long = 0) {
        view.animate().alpha(toAlpha).setDuration(duration).setStartDelay(delay).start()
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
