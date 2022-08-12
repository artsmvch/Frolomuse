package com.frolo.core.ui.marker

import android.content.Intent


fun interface IntentHandler {
    fun handleIntent(intent: Intent): Boolean
}