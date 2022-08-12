package com.frolo.muse.ui.main

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider


internal class PlayerSheetOutlineProvider(
    var cornerRadius: Float = 0f
): ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(0, 0, view.width,
            view.measuredHeight + cornerRadius.toInt(), cornerRadius)
    }
}