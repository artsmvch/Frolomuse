package com.frolo.muse.ui.main.settings.theme

import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.frolo.core.ui.setOverScrollModeCompat
import com.frolo.muse.android.activityManager
import kotlin.math.abs
import kotlin.math.min


class ThemePageCarouselHelper private constructor(
    private val viewPager: ViewPager2
): ViewTreeObserver.OnGlobalLayoutListener {

    private val previewWidthPercent = 0.12f

    override fun onGlobalLayout() {
        val pagerWidth = viewPager.measuredWidth
        val pagerHeight = viewPager.measuredHeight

        if (pagerWidth > 0 && pagerHeight > 0) {
            // We're done with measurement
            viewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)
        } else {
            // The width or the height is invalid (<= 0)
            return
        }

        val horizontalPadding: Int = (pagerWidth * previewWidthPercent).toInt()
        val targetContentWidth: Int = pagerWidth - 2 * horizontalPadding
        val targetContentHeight: Int = min(pagerHeight, 2 * targetContentWidth)
        val verticalPadding: Int = (pagerHeight / targetContentHeight).coerceAtLeast(0) / 2
        viewPager.updatePadding(
            left = horizontalPadding,
            top = verticalPadding,
            right = horizontalPadding,
            bottom = verticalPadding
        )

        val compositePageTransformer = CompositePageTransformer()
        // A dirty hack to prevent the next and previous pages from disappearing
        // when scrolling the view pager. Idk how this works but it's necessary.
        compositePageTransformer.addTransformer(MarginPageTransformer(1))
        compositePageTransformer.addTransformer(ThemePageTransformer())
        viewPager.setPageTransformer(compositePageTransformer)
    }

    companion object {

        fun setup(target: ViewPager2) {
            // For not-low ram devices we can show up to 8 pages
            val maxOffscreenPageLimit = target.context.activityManager.let { manager ->
                if (manager != null && !manager.isLowRamDevice) 8 else 1
            }
            with(target) {
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = maxOffscreenPageLimit
                setOverScrollModeCompat(ViewPager2.OVER_SCROLL_NEVER)
            }

            val layoutListener = ThemePageCarouselHelper(target)

            target.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
                }

                override fun onViewDetachedFromWindow(view: View) {
                    view.removeOnAttachStateChangeListener(this)
                    view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
                }
            })

            if (target.isAttachedToWindow) {
                target.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
            }
        }

    }

}

private class ThemePageTransformer : ViewPager2.PageTransformer {

    private val baseScale = 0.93f
    private val maxScale = 1f
    private val baseAlpha = 0.6f
    private val maxAlpha = 1f
    private val changePosThreshold = 0.8f

    override fun transformPage(page: View, position: Float) {
        when {
            // [-Infinity,-1)
            position < -1f -> {
                setBaseValues(page)
            }

            // [-1,1]
            position <= 1f -> {
                val absPosition = abs(position)
                if (absPosition < changePosThreshold) {
                    val scaleValue = calculateChangeFactor(baseScale, maxScale, absPosition, changePosThreshold)
                    page.scaleX = scaleValue
                    page.scaleY = scaleValue
                    page.alpha = calculateChangeFactor(baseAlpha, maxAlpha, absPosition, changePosThreshold)
                } else {
                    setBaseValues(page)
                }

            }

            // (1,+Infinity]
            else -> {
                setBaseValues(page)
            }
        }
    }

    private fun setBaseValues(page: View) {
        page.scaleX = baseScale
        page.scaleY = baseScale
        page.alpha = baseAlpha
    }

    private fun calculateChangeFactor(
            baseValue: Float, maxValue: Float, absPosition: Float, changePosThreshold: Float): Float {
        return maxValue - (maxValue - baseValue) * (absPosition / changePosThreshold)
    }

}