package com.frolo.muse.ui

import androidx.fragment.app.FragmentManager


fun FragmentManager.removeAllFragmentsNow() {
    val transaction = beginTransaction()

    fragments.forEach { fragment ->
        transaction.remove(fragment)
    }

    transaction.commitNow()
}