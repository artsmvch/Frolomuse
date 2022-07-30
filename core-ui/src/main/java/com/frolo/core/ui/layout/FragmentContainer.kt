package com.frolo.core.ui.layout

import android.content.Context
import android.util.AttributeSet
import android.view.WindowInsets


class FragmentContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): DrawingSystemBarsLayout(context, attrs, defStyleAttr) {

    private var onApplyWindowInsetsListener: OnApplyWindowInsetsListener? = null

    override fun setOnApplyWindowInsetsListener(listener: OnApplyWindowInsetsListener?) {
        super.setOnApplyWindowInsetsListener(listener)
        onApplyWindowInsetsListener = listener
    }

    override fun dispatchApplyWindowInsetsImpl(insets: WindowInsets): WindowInsets {
        val listener = onApplyWindowInsetsListener
        if (listener != null) {
            return listener.onApplyWindowInsets(this, insets)
        }
        return super.dispatchApplyWindowInsetsImpl(insets)
    }
}