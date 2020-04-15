package com.frolo.muse.views.text

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.frolo.muse.R


/**
 * TextSwitcher that allows clients to change text color without pain.
 */
class AppTextSwitcher @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.appTextSwitcherStyle
): TextSwitcher(context, attrs) {

    @ColorInt
    private var textColor: Int? = null

    @StyleRes
    private var textAppearanceResId: Int? = null

    /**
     * TextView that is being displayed right now
     */
    private var currTextView: TextView? = null

    init {
        val a = context.theme
                .obtainStyledAttributes(attrs, R.styleable.AppTextSwitcher, defStyleAttr, 0)
        if (a.hasValue(R.styleable.AppTextSwitcher_textColor)) {
            textColor = a.getColor(R.styleable.AppTextSwitcher_textColor, Color.BLACK)
        }
        if (a.hasValue(R.styleable.AppTextSwitcher_textAppearance)) {
            textAppearanceResId = a.getResourceId(R.styleable.AppTextSwitcher_textAppearance, 0)
        }
        a.recycle()
    }

    fun setTextColor(@ColorInt color: Int) {
        textColor = color
        currTextView?.setTextColor(color)
    }

    fun setTextAppearance(@StyleRes resId: Int) {
        textAppearanceResId = resId
        currTextView?.setTextAppearanceCompat(resId)
    }

    override fun setFactory(factory: ViewFactory?) {
        val finalFactory = factory?.let { ProxyFactory(it) }
        super.setFactory(finalFactory)
    }

    /**
     * Proxy factory that delegates the creation of views to [delegate] factory.
     */
    private inner class ProxyFactory constructor(
        val delegate: ViewFactory
    ): ViewFactory {

        override fun makeView(): View {
            val view = delegate.makeView()

            if (view is TextView) {
                currTextView = view

                textAppearanceResId?.also { safeResId ->
                    view.setTextAppearanceCompat(safeResId)
                }

                textColor?.also { safeTextColor ->
                    view.setTextColor(safeTextColor)
                }
            }

            return view
        }

    }

    companion object {

        private fun TextView.setTextAppearanceCompat(@StyleRes resId: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextAppearance(resId)
            } else {
                setTextAppearance(context, resId)
            }
        }

    }

}