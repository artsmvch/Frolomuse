package com.frolo.muse.views.spring

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.core.view.updatePadding
import androidx.recyclerview.R
import com.l4digital.fastscroll.FastScrollRecyclerView
import com.l4digital.fastscroll.FastScroller


/**
 * Same as [SpringRecyclerView], but in addition the fast scroll is implemented.
 */
class AppRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.recyclerViewStyle
) : FastScrollRecyclerView(context, attrs, defStyleAttr) {

    private val springManager = SpringEdgeEffect.Manager(this)

    private var shouldTranslateSelf = true

    private var isTopFadingEdgeEnabled = true

    private val fastScroller: FastScroller? by lazy {
        try {
            val field = FastScrollRecyclerView::class.java.getDeclaredField("fastScroller")
            field.isAccessible = true
            field.get(this) as FastScroller
        } catch (ignored: Throwable) {
            null
        }
    }

    init {
        edgeEffectFactory = springManager.createFactory()
        itemAnimator?.setupDurationsByDefault()
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

    override fun setClipToPadding(clipToPadding: Boolean) {
        super.setClipToPadding(clipToPadding)
        fastScroller?.clipToPadding = clipToPadding
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        fastScroller?.updatePadding(
            left = left,
            top = top,
            right = right,
            bottom = bottom
        )
    }
}
