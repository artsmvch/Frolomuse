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

    // The current timer.
    private var timer: Timer? = null

    // Indicates whether the current timer has been pulsed at least once.
    // This flag is set to false when the timer is scheduled/cancelled.
    private var pulsed: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when(event.action) {
            MotionEvent.ACTION_DOWN -> {

                // Cancel the previous timer, if any
                timer?.apply {
                    cancel()
                    purge()
                }

                pulsed = false
                timer = Timer("pulse_touch_down").also { timer ->
                    timer.schedule(object : TimerTask() {
                        override fun run() {
                            v.post {
                                if (v.isAttachedToWindow) {
                                    pulsed = true
                                    onPulse.invoke()
                                } else {
                                    cancel()
                                }
                            }
                        }
                    }, delay, period)
                }

                false
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_CANCEL -> {
                // The result is the value of the 'pulsed' flag.
                // If the timer has not been pulsed at all, then the result is false and the touch is handled by default.
                // Otherwise, the result is true.
                val result = pulsed

                timer?.apply {
                    cancel()
                    purge()
                }
                pulsed = false

                if (result) {
                    // Clear the pressed state
                    v.isPressed = false
                }

                result
            }

            else -> false
        }
    }
}