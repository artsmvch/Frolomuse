package com.frolo.muse.ui.main.library

import com.google.android.material.floatingactionbutton.FloatingActionButton


// Fragments should implement this interface if they want to have a FAB
interface FabCallback {
    fun isUsingFab(): Boolean
    fun decorateFab(fab: FloatingActionButton)
    fun handleClickOnFab()
}