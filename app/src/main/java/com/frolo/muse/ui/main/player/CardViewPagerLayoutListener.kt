package com.frolo.muse.ui.main.player

import android.view.View
import android.view.ViewTreeObserver
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2


// Listens to layout changes of a view pager to setup card transformation on it.
class CardViewPagerLayoutListener private constructor(
        private val viewPager: ViewPager2
) : ViewTreeObserver.OnGlobalLayoutListener {

    companion object {
        fun setup(target: ViewPager2) {

            val layoutListener = CardViewPagerLayoutListener(target)

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

    override fun onGlobalLayout() {
        // remove itself from listener as the viewpager laid out
        viewPager.viewTreeObserver.removeOnGlobalLayoutListener(this)

        // it's supposed to have at least card transformer.
        // but also it may require a margin page transformer.
        // so it needs to be a composite transformer.
        val transformer = CompositePageTransformer()

        // min scale for page transformation
        val minPageScale: Float = 0.75f
        // max scale for page transformation
        val maxPageScale: Float = 1.0f
        // the visible part of previews relatively in percentage
        val previewPageVisiblePart: Float = 0.2f

        val width = viewPager.measuredWidth.toFloat()
        val height = viewPager.measuredHeight.toFloat()
        val actualRatio = width / height

        // Here, we need to calculate each padding
        val pageSizeRel = 1f
        val previewPageWidthRel = pageSizeRel * previewPageVisiblePart// * minPageScale
        // the perfect width relative
        val widthRel = pageSizeRel + 2 * previewPageWidthRel
        // the perfect height relative
        val heightRel = pageSizeRel

        val perfectRatio = widthRel / heightRel
        if (actualRatio < perfectRatio) {
            // OK, the width is perfect but not the height...
            // Need to add vertical padding.

            val desiredHeight = width / perfectRatio
            val hp = ((width - desiredHeight) / 2).toInt()
            val vp = ((height - desiredHeight) / 2).toInt()
            viewPager.setPadding(hp, vp, hp, vp)
        } else if (actualRatio >= perfectRatio) {
            // OK, the height is perfect but not the width...
            // Need to add horizontal padding.

            val pageSize = height // because the page fits the height without additional padding
            val hp = ((width - pageSize) / 2).toInt()
            viewPager.setPadding(hp, 0, hp, 0)

            // Visible part of preview may still be too big.
            // Need to add page margin.
            val desiredWidth = height * perfectRatio
            val margin = ((width - desiredWidth) / 2).toInt()

            transformer.addTransformer(MarginPageTransformer(margin))
        }

        transformer.addTransformer(CardTransformer(minPageScale, maxPageScale))

        viewPager.setPageTransformer(transformer)
    }

}