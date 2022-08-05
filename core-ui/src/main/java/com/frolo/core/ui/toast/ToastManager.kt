package com.frolo.core.ui.toast

interface ToastManager {
    fun showToastMessage(message: CharSequence)
    fun showToastMessage(error: Throwable)

    object NONE : ToastManager {
        override fun showToastMessage(message: CharSequence) = Unit
        override fun showToastMessage(error: Throwable) = Unit
    }
}