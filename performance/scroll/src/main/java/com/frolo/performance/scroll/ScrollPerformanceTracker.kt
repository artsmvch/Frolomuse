package com.frolo.performance.scroll

import android.view.Choreographer
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit


class ScrollPerformanceTracker private constructor(
    private val listView: RecyclerView,
    private val callback: ScrollPerformanceCallback
){
    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING,
                RecyclerView.SCROLL_STATE_SETTLING -> {
                    onScrollStarted()
                }
                RecyclerView.SCROLL_STATE_IDLE -> {
                    onScrollStopped()
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            onScrolled()
        }
    }

    private val frameCallback = Choreographer.FrameCallback { frameTimeNanos ->
        onFrameCallback(frameTimeNanos)
    }
    private var isScrolling: Boolean = false
    private var lastFrameTimeNanos: Long? = null

    private val state: StateImpl = StateImpl(
        renderTimeNanos = 0L
    )

    private fun attach() {
        listView.addOnScrollListener(scrollListener)
    }

    private fun detach() {
        listView.removeOnScrollListener(scrollListener)
    }

    private fun onScrollStarted() {
        isScrolling = true
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    private fun onScrollStopped() {
        isScrolling = false
        lastFrameTimeNanos = null
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun onScrolled() {
    }

    private fun onFrameCallback(frameTimeNanos: Long) {
        lastFrameTimeNanos?.also { lastTimeNanos ->
            val renderTimeNanos = frameTimeNanos - lastTimeNanos
            if (renderTimeNanos > NORMAL_RENDER_TIME_NANOS) {
                state.renderTimeNanos = renderTimeNanos
                callback.onPoorScrollPerformance(listView, state)
            }
        }
        lastFrameTimeNanos = frameTimeNanos

        if (isScrolling){
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }
    }

    private class StateImpl(
        override var renderTimeNanos: Long,
    ) : ScrollPerformanceInfo

    companion object {
        private val NORMAL_RENDER_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(18L)

        fun attach(listView: RecyclerView, callback: ScrollPerformanceCallback) {
            ScrollPerformanceTracker(listView, callback).attach()
        }
    }
}