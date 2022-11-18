package com.frolo.core.ui.tabs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.frolo.core.ui.R
import com.frolo.debug.DebugUtils
import com.frolo.ui.StyleUtils
import com.google.android.material.tabs.TabLayout


class CustomTabLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.tabStyle
): TabLayout(context, attrs, defStyleAttr) {

    override fun newTab(): Tab {
        val newTab = super.newTab()
        newTab.customView = CustomTabView(context)
        return newTab
    }

    private class CustomTabView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ): FrameLayout(context, attrs, defStyleAttr) {

        private var titleView: TextView? = null
        private val selectedAlpha: Float = 1f
        private val unselectedAlpha: Float =
            if (StyleUtils.resolveBool(context, com.google.android.material.R.attr.isLightTheme)) 0.68f else 0.4f

        init {
            val viewContext = this.context
            View.inflate(viewContext, R.layout.tab, this)
            titleView = findViewById(android.R.id.text1)
            titleView?.also { view ->
                val targetScale = getTargetScaleForSelection(isSelected)
                view.scaleX = targetScale
                view.scaleY = targetScale
                view.alpha = getTargetAlphaForSelection(isSelected)
            }
            kotlin.runCatching {
                val tabStyleId = StyleUtils.resolveStyleRes(context, com.google.android.material.R.attr.tabStyle)
                val tabStyleContext = ContextThemeWrapper(context, tabStyleId)
                val textAppearanceId = StyleUtils.resolveStyleRes(
                    tabStyleContext, com.google.android.material.R.attr.tabTextAppearance)
                titleView?.setTextAppearance(textAppearanceId)
            }.onFailure { err ->
                DebugUtils.dump(err)
            }
        }

        override fun dispatchSetSelected(selected: Boolean) {
            super.dispatchSetSelected(selected)
            titleView?.also { view ->
                animateTitleView(view, selected)
            }
        }

        private fun getTargetScaleForSelection(selected: Boolean): Float {
            return if (selected) 1.0f else 0.8f
        }

        private fun getTargetAlphaForSelection(selected: Boolean): Float {
            return if (selected) selectedAlpha else unselectedAlpha
        }

        private fun animateTitleView(view: View, selected: Boolean) {
            val targetScale = getTargetScaleForSelection(selected)
            view.animate()
                .scaleX(targetScale)
                .scaleY(targetScale)
                .alpha(getTargetAlphaForSelection(selected))
                .setDuration(150L)
                .setInterpolator(titleAnimationInterpolator)
                .start()
        }

        private companion object {
            private val titleAnimationInterpolator = AccelerateDecelerateInterpolator()
        }
    }
}