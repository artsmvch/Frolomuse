package com.frolo.muse.ui.main.player

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout


class AppCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): CoordinatorLayout(context, attrs, defStyleAttr) {

    var interceptTouches: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                parent?.requestDisallowInterceptTouchEvent(value)
            }
        }

}