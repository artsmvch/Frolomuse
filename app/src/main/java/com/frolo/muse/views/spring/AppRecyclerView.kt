package com.frolo.muse.views.spring

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.frolo.muse.R
import com.frolo.muse.StyleUtil
import com.l4digital.fastscroll.FastScroller


/**
 * Customized RecyclerView that implements fast scroll and spring over scroll.
 */
class AppRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private val springManager = SpringEdgeEffect.Manager(this)

    private var shouldTranslateSelf = true

    private var isTopFadingEdgeEnabled = true

    private val fastScroller: FastScroller

    init {
        fastScroller = FastScroller(fastScrollerContext(context), attrs)
        fastScroller.id = com.l4digital.fastscroll.R.id.fast_scroller

        edgeEffectFactory = springManager.createFactory()

        (itemAnimator as? SimpleItemAnimator)?.apply {
            addDuration = 100
            removeDuration = 100
            moveDuration = 150
            changeDuration = 150
        }
    }

    override fun draw(canvas: Canvas) {
        springManager.withSpring(canvas, shouldTranslateSelf) {
            super.draw(canvas)
            false
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        springManager.withSpring(canvas, !shouldTranslateSelf) {
            super.dispatchDraw(canvas)
            false
        }
    }

    override fun getTopFadingEdgeStrength(): Float {
        return if (isTopFadingEdgeEnabled) super.getTopFadingEdgeStrength() else 0f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        fastScroller.attachRecyclerView(this)
    }

    override fun onDetachedFromWindow() {
        fastScroller.detachRecyclerView()
        super.onDetachedFromWindow()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        if (adapter is FastScroller.SectionIndexer) {
            fastScroller.setSectionIndexer(adapter as FastScroller.SectionIndexer?)
        } else if (adapter == null) {
            fastScroller.setSectionIndexer(null)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        fastScroller.visibility = visibility
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        fastScroller.updateLayoutParams<MarginLayoutParams> {
            topMargin += top
            bottomMargin += bottom
        }
    }

    companion object {

        private fun fastScrollerContext(context: Context): Context {
            val fastScrollerThemeId =
                    StyleUtil.resolveStyleRes(context, com.frolo.muse.R.attr.fastScrollerStyle)
            return if (fastScrollerThemeId != 0) ContextThemeWrapper(context, fastScrollerThemeId)
            else ContextThemeWrapper(context, R.style.Base_AppTheme_FastScroller)
        }

    }

}
