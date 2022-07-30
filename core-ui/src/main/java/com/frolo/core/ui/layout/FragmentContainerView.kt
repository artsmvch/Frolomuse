package com.frolo.core.ui.layout

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets


class FragmentContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): DrawingSystemBarsLayout(context, attrs, defStyleAttr) {

    private var applyWindowInsetsListener: OnApplyWindowInsetsListener? = null

    override fun setOnApplyWindowInsetsListener(listener: OnApplyWindowInsetsListener?) {
        applyWindowInsetsListener = listener
    }

    override fun dispatchApplyWindowInsetsImpl(insets: WindowInsets): WindowInsets {
        val listener = applyWindowInsetsListener
        if (listener != null) {
            return listener.onApplyWindowInsets(this, insets)
        }
        return super.dispatchApplyWindowInsetsImpl(insets)
    }
}