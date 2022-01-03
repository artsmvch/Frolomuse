package com.frolo.muse.util

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.airbnb.lottie.LottieAnimationView
import com.frolo.muse.Logger
import java.lang.ref.WeakReference


class SimpleLottieAnimationController(fragment: Fragment) {

    private val fragmentRef = WeakReference(fragment)

    private val activity: Activity? get() = fragmentRef.get()?.activity

    // The current lottie animation view playing the animation
    private var currAnimationView: LottieAnimationView? = null

    // The window manager to which the current animation view was added
    private var currWindowManager: WindowManager? = null

    private val fragmentLifecycleObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    removeAnimationView()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    removeAnimationView()
                    source.lifecycle.removeObserver(this)
                }
                else -> Unit
            }
        }
    }

    init {
        if (isAlive(fragment)) {
            fragment.lifecycle.addObserver(fragmentLifecycleObserver)
        }
    }

    private fun isAlive(fragment: Fragment): Boolean {
        return fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)
    }

    private fun getWindowManager(): WindowManager? {
        return activity?.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    }

    private fun createAnimationView(): LottieAnimationView {
        val animationView = object : LottieAnimationView(activity) {
            override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
                return activity?.dispatchKeyEvent(event) ?: false
            }
        }
        animationView.scaleType = ImageView.ScaleType.CENTER_CROP
        animationView.setFailureListener { error -> Logger.e(error) }
        animationView.addAnimatorListener(
            object : Animator.AnimatorListener {

                var isOrWillBeCleared = false

                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    clearThisAnimationView()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    clearThisAnimationView()
                }

                private fun clearThisAnimationView() {
                    if (isOrWillBeCleared) {
                        // Safe way to make sure it is called only once
                        return
                    }
                    // We done
                    isOrWillBeCleared = true
                    animationView.removeAnimatorListener(this)
                    if (this@SimpleLottieAnimationController.currAnimationView == animationView) {
                        removeAnimationView()
                    }
                }
            }
        )

        // Window insets
        animationView.fitsSystemWindows = false
        ViewCompat.setOnApplyWindowInsetsListener(animationView) { _, _ ->
            WindowInsetsCompat.CONSUMED
        }

        return animationView
    }

    private fun createLayoutParams(windowToken: IBinder): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
        layoutParams.token = windowToken
        layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        layoutParams.windowAnimations = 0
        layoutParams.format = PixelFormat.TRANSLUCENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.gravity = Gravity.NO_GRAVITY

        layoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        return layoutParams
    }

    private fun removeAnimationView() {
        currWindowManager?.also { safeWindowManager ->
            currAnimationView?.also { safeAnimationView ->
                safeAnimationView.cancelAnimation()
                safeWindowManager.removeView(safeAnimationView)
            }
        }
        currWindowManager = null
        currAnimationView = null
    }

    fun playAnimation(assetName: String) {
        // Remove the old one
        removeAnimationView()

        val fragment = fragmentRef.get()
        if (fragment == null || !isAlive(fragment)) {
            return
        }

        // Add a new one
        val windowManager = getWindowManager() ?: return
        val windowToken = activity?.window?.decorView?.windowToken ?: return
        val animationView = createAnimationView()
        val layoutParams = createLayoutParams(windowToken)
        windowManager.addView(animationView, layoutParams)
        currWindowManager = windowManager
        currAnimationView = animationView

        // Setup and play the animation
        animationView.setAnimation(assetName)
        animationView.playAnimation()
    }
}