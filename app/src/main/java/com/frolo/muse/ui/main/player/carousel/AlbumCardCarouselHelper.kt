package com.frolo.muse.ui.main.player.carousel

import android.view.View
import android.view.ViewTreeObserver
import androidx.viewpager2.widget.ViewPager2
import com.frolo.ui.Screen
import com.frolo.muse.views.viewpager.setOverScrollModeCompat
import kotlinx.android.synthetic.main.include_square_album_art.view.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow


class AlbumCardCarouselHelper private constructor(
    private val viewPager: ViewPager2
) : ViewTreeObserver.OnGlobalLayoutListener {

    private val minPageScale: Float = 0.75f
    private val maxPageScale: Float = 1f

    // For debugging
    private val screenLayoutSizeName: String by lazy {
        Screen.getLayoutSizeName(viewPager.context)
    }

    /**
     * Minimal card elevation that all items must have on their album cards.
     */
    private val baseCardElevation: Float by lazy {
        AlbumCardProperties.getBaseCardElevation(viewPager.context)
    }

    /**
     * Additional card elevation for the currently selected item.
     */
    private val raisingCardElevation: Float by lazy {
        AlbumCardProperties.getRaisingCardElevation(viewPager.context)
    }

    /**
     * The maximum percent of the page width relatively to the pager width.
     * This value must be such that the previews of the left and right pages are visible enough.
     */
    private val maxPageWidthPercent: Float = 0.85f

    // Internal state
    private var lastPagerWidth: Int = -1
    private var lastPagerHeight: Int = -1

    override fun onGlobalLayout() {
        val pagerWidth = viewPager.measuredWidth
        val pagerHeight = viewPager.measuredHeight

        // Check if the size has changed
        if (lastPagerWidth == pagerWidth && lastPagerHeight == pagerHeight) {
            return
        }
        lastPagerWidth = pagerWidth
        lastPagerHeight = pagerHeight

        updateTransformer(pagerWidth, pagerHeight)
    }

    private fun updateTransformer(pagerWidth: Int, pagerHeight: Int) {
        val initialPageWidth = (pagerWidth * maxPageWidthPercent).toInt()
        val initialPageHeight = initialPageWidth

        val finalPageHeight = min(pagerHeight, initialPageHeight)
        val finalPageWidth = finalPageHeight

        val hp = (pagerWidth - finalPageWidth) / 2
        val vp = (pagerHeight - finalPageHeight) / 2

        viewPager.setPadding(hp, vp, hp, vp)

        val transformer = AlbumCardTransformer(
            minScale = minPageScale,
            maxScale = maxPageScale,
            baseCardElevation = baseCardElevation,
            raisingCardElevation = raisingCardElevation
        )
        viewPager.setPageTransformer(transformer)
    }

    companion object {
        fun setup(target: ViewPager2) {

            with(target) {
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 1
                setOverScrollModeCompat(ViewPager2.OVER_SCROLL_NEVER)
            }

            val layoutListener = AlbumCardCarouselHelper(target)

            target.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
                }

                override fun onViewDetachedFromWindow(view: View) {
                    // stop listening to attach state changes
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

private class AlbumCardTransformer constructor(
    private val minScale: Float,
    private val maxScale: Float,
    private val baseCardElevation: Float,
    private val raisingCardElevation: Float
) : ViewPager2.PageTransformer {

    override fun transformPage(page: View, position: Float) {
        val safePosition = when {
            position < -1f -> -1f
            position > 1f -> 1f
            else -> position
        }

        val tempScale = if (safePosition < 0) (1 + safePosition) else (1 - safePosition)

        val slope = (maxScale - minScale) / 1
        val scaleValue = minScale + tempScale * slope

        val absOffset = (page.measuredWidth * (1 - scaleValue)) / 2
        val offset = if (position < 0f) absOffset else -absOffset

        // translation offset makes it closer to the currently selected page
        page.translationX = offset

        page.scaleX = scaleValue
        page.scaleY = scaleValue

        page.cv_album_art.cardElevation = calculateCardElevation(position)
    }

    private fun calculateCardElevation(position: Float): Float {
        val offsetCoefficient = 1f - min(1f, abs(position))
        val raisingElevationCoefficient = offsetCoefficient.pow(3)
        return baseCardElevation + raisingCardElevation * raisingElevationCoefficient
    }

}