package com.frolo.muse.ui.main.player.carousel

import android.view.View
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.frolo.muse.toPx
import kotlinx.android.synthetic.main.include_square_album_art.view.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow


class AlbumCardCarouselHelper private constructor(
    private val viewPager: ViewPager2
) : ViewTreeObserver.OnGlobalLayoutListener {

    private val minPageScale: Float = 0.75f
    private val maxPageScale: Float = 1f

    /**
     * Minimal card elevation that all items must have on their album cards.
     */
    private val baseCardElevation: Float = 4f.toPx(viewPager.context)
    /**
     * Additional card elevation for the currently selected item.
     */
    private val raisingCardElevation: Float = 12f.toPx(viewPager.context)

    /**
     * The maximum percent of the page width relatively to the pager width.
     * This value must be such that the previews of the left and right pages are visible enough.
     */
    private val maxPageWidthPercent: Float = 0.7f

    override fun onGlobalLayout() {
        // remove itself from listener as the viewpager laid out
        viewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)

        val pagerWidth = viewPager.measuredWidth
        val pagerHeight = viewPager.measuredHeight

        val initialPageWidth = (pagerWidth * maxPageWidthPercent).toInt()
        val initialPageHeight = initialPageWidth

        val finalPageHeight = min(pagerHeight, initialPageHeight)
        val finalPageWidth = finalPageHeight

        val hp = (pagerWidth - finalPageWidth) / 2
        val vp = (pagerHeight - finalPageHeight) / 2

        viewPager.setPadding(hp, vp, hp, vp)

        AlbumCardTransformer(
            minScale = minPageScale,
            maxScale = maxPageScale,
            baseCardElevation = baseCardElevation,
            raisingCardElevation = raisingCardElevation
        ).also { transformer ->
            viewPager.setPageTransformer(transformer)
        }
    }

    companion object {
        fun setup(target: ViewPager2) {

            with(target) {
                clipToPadding = false
                clipChildren = false
                overScrollMode = androidx.viewpager2.widget.ViewPager2.OVER_SCROLL_NEVER
                offscreenPageLimit = 1
                (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
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