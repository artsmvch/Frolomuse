package com.frolo.muse.ui.main.player

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import androidx.annotation.UiThread
import com.frolo.muse.rx.subscribeSafely
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


@UiThread
class PulseTouchDownListener constructor(
    private val initialDelay: Long,
    private val period: Long,
    private val onPulse: () -> Unit
): View.OnTouchListener {

    // The current timer disposable.
    private var timerDisposable: Disposable? = null

    // Indicates whether the current timer has been pulsed at least once.
    // This flag is set to false when the timer is scheduled/cancelled.
    private var pulsed: Boolean = false

    val isRunning: Boolean
        get() {
            val disposable: Disposable? = timerDisposable
            return disposable != null && !disposable.isDisposed
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        return when(event.action) {
            MotionEvent.ACTION_DOWN -> {

                // Cancel the previous timer, if any
                timerDisposable?.dispose()

                pulsed = false

                timerDisposable = Observable.interval(initialDelay, period, TimeUnit.MILLISECONDS)
                    .timeInterval()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        if (view.isAttachedToWindow) {
                            pulsed = true
                            onPulse.invoke()
                        } else {
                            timerDisposable?.dispose()
                            timerDisposable = null
                        }
                    }
                    .subscribeSafely()

                false
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_OUTSIDE, MotionEvent.ACTION_CANCEL -> {
                // The result is the value of the 'pulsed' flag.
                // If the timer has not been pulsed at all, then the result is false and the touch is handled by default.
                // Otherwise, the result is true.
                val result: Boolean = pulsed

                timerDisposable?.dispose()
                timerDisposable = null

                pulsed = false

                if (result) {
                    // Clear the pressed state
                    view.isPressed = false
                }

                result
            }

            else -> false
        }
    }
}

fun View.doOnPulseTouchDown(initialDelay: Long = 500, period: Long = 750, action: () -> Unit) {
    setOnTouchListener(PulseTouchDownListener(initialDelay, period, action))
}