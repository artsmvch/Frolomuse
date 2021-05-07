package com.frolo.muse.views.viewpager

import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2


fun ViewPager2.setOverScrollModeCompat(mode: Int) {
    overScrollMode = mode
    underlyingRecyclerView?.overScrollMode = mode
}

val ViewPager2.underlyingRecyclerView: RecyclerView?
    get() {
        return getChildAt(0) as? RecyclerView
    }