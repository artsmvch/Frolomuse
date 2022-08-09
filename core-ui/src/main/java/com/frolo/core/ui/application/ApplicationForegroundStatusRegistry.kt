package com.frolo.core.ui.application

interface ApplicationForegroundStatusRegistry {
    val isInForeground: Boolean
    val isInBackground: Boolean

    fun addObserver(observer: Observer)
    fun removeObserver(observer: Observer)

    fun interface Observer {
        fun onApplicationForegroundStatusChanged(isInForeground: Boolean)
    }
}