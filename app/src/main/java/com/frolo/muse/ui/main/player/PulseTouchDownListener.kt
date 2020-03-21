package com.frolo.muse.ui.main.player

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import java.util.*


class PulseTouchDownListener constructor(
    private val delay: Long,
    private val period: Long,
    private val onPulse: () -> Unit
): View.OnTouchListener {
    private var holdingTouchDown = false
    private var timer: Timer? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                timer?.apply {
                    cancel()
                    purge()
                }
                timer = Timer().also { timer ->
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            holdingTouchDown = true
                            v.post {
                                if (v.isAttachedToWindow) {
                                    onPulse()
                                } else {
                                    holdingTouchDown = false
                                    cancel()
                                }
                            }
                        }
                    }, delay, period)
                }
                false
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_CANCEL -> {
                if (!holdingTouchDown) {
                    timer?.apply {
                        cancel()
                        purge()
                    }
                    false
                } else {
                    holdingTouchDown = false
                    timer?.apply {
                        cancel()
                        purge()
                    }
                    true
                }
            }

            else -> false
        }
    }
}