package com.frolo.muse.ui

import android.content.Intent


fun interface IntentHandler {
    fun handleIntent(intent: Intent): Boolean
}