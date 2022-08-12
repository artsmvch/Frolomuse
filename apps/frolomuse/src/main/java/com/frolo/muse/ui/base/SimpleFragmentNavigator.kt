package com.frolo.muse.ui.base


interface SimpleFragmentNavigator {
    /**
     * Pushes [newFragment] to the top of the current fragment stack.
     */
    fun pushFragment(newFragment: androidx.fragment.app.Fragment)

    /**
     * Pushes [newDialog]. It will be shown on top of all other fragments, just like a usual dialog.
     */
    fun pushDialog(newDialog: androidx.fragment.app.DialogFragment)

    /**
     * Pops the top fragment off the current stack. If there is a dialog fragment shown, then it is cleared first.
     */
    fun pop()
}