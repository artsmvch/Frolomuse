package com.frolo.muse.views.spring

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator


fun RecyclerView.ItemAnimator.setupDurationsByDefault() {
    (this as? SimpleItemAnimator)?.apply {
        addDuration = 100
        removeDuration = 100
        moveDuration = 150
        changeDuration = 150
    }
}