package com.frolo.muse.ui.base


interface FragmentNavigator {
    fun pushFragment(newFragment: androidx.fragment.app.Fragment)
    fun pushDialog(newDialog: androidx.fragment.app.DialogFragment)
    fun pop()
}