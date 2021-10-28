package com.frolo.muse.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView


class SongThumbnailView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatImageView(context, attrs, defStyleAttr) {

    private val dimPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            color = Color.WHITE
        }
    }

    var isDimmed: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                alpha = if (value) 0.32f else 1f
                invalidate()
            }
        }

}