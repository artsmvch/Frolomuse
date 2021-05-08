package com.frolo.muse.ui.main

import androidx.fragment.app.DialogFragment


/**
 * Returns true if the dialog fragment is actually showing.
 */
val DialogFragment.isShowing: Boolean
    get() {
        return dialog?.isShowing == true && !isRemoving
    }