package com.frolo.core.ui.actionmode

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextSwitcher
import androidx.annotation.UiContext
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.AppCompatTextView
import com.frolo.core.ui.R
import com.google.android.material.R as MaterialR
import com.frolo.ui.StyleUtils


class ActionModeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(wrapContext(context), attrs, defStyleAttr) {

    private val closeButton: View
    private val titleSwitcher: TextSwitcher
    private val actionMenuView: ActionMenuView

    init {
        inflate(context, R.layout.merge_action_mode, this)
        closeButton = findViewById(R.id.close)
        titleSwitcher = findViewById<TextSwitcher>(R.id.title_switcher).also(::setupTitleSwitcher)
        actionMenuView = findViewById(R.id.action_menu)
        background = StyleUtils.resolveDrawable(super.getContext(), MaterialR.attr.actionModeBackground)
    }

    var title: CharSequence? = null
        set(value) {
            field = value
            titleSwitcher.setText(value)
        }

    var subtitle: CharSequence? = null
        set(value) {
            field = value
        }

    val menu: Menu get() = actionMenuView.menu

    private fun setupTitleSwitcher(switcher: TextSwitcher) {
        switcher.setFactory {
            val styleId = StyleUtils.resolveStyleRes(context, MaterialR.attr.titleTextStyle)
            AppCompatTextView(context).apply {
                setTextAppearance(styleId)
                gravity = Gravity.CENTER
                ellipsize = TextUtils.TruncateAt.END
            }
        }
        switcher.setInAnimation(context, R.anim.action_mode_title_in_animation)
        switcher.setOutAnimation(context, R.anim.action_mode_title_out_animation)
    }

    fun setOnCloseClickListener(listener: View.OnClickListener?) {
        closeButton.setOnClickListener(listener)
    }

    fun setOnMenuItemClickListener(listener: (MenuItem) -> Boolean) {
        actionMenuView.setOnMenuItemClickListener(listener)
    }

    fun show() {
        visibility = View.VISIBLE
        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(VISIBILITY_ANIM_DURATION)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    fun hide() {
        animate()
            .alpha(0f)
            .setDuration(VISIBILITY_ANIM_DURATION)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                visibility = View.GONE
            }
            .start()
    }

    companion object {
        const val VISIBILITY_ANIM_DURATION = 250L

        @UiContext
        private fun wrapContext(context: Context): Context {
            val actionModeStyle = StyleUtils.resolveStyleRes(context, MaterialR.attr.actionModeStyle)
            return ContextThemeWrapper(context, actionModeStyle)
        }
    }
}