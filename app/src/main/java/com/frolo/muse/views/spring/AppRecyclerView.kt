package com.frolo.muse.views.spring

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.R
import com.frolo.muse.StyleUtil
import com.l4digital.fastscroll.FastScrollRecyclerView
import com.l4digital.fastscroll.FastScroller


/**
 * Same as [SpringRecyclerView], but in addition the fast scroll is implemented.
 */
class AppRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.recyclerViewStyle
) : FastScrollRecyclerView(
    ContextThemeWrapper(context, StyleUtil.readStyleAttrValue(context, com.frolo.muse.R.attr.fastScrollerStyle)),
    attrs,
    defStyleAttr
) {

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

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        fastScroller?.updateLayoutParams<MarginLayoutParams> {
            topMargin += top
            bottomMargin += bottom
        }
    }

}
