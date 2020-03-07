package com.frolo.muse.views.text

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.annotation.ColorInt


/**
 * TextSwitcher that allows clients to change text color without pain.
 */
class AppTextSwitcher @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): TextSwitcher(context, attrs) {

    @ColorInt
    private var textColor: Int? = null

    /**
     * TextView that is being displayed right now
     */
    private var currTextView: TextView? = null

    fun setTextColor(@ColorInt color: Int) {
        textColor = color
        currTextView?.setTextColor(color)
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

                textColor?.also { safeTextColor ->
                    view.setTextColor(safeTextColor)
                }
            }

            return view
        }

    }

}