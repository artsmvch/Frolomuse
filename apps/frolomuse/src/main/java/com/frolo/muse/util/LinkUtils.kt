package com.frolo.muse.util

import android.content.ContentResolver

object LinkUtils {
    fun isHttpScheme(scheme: String): Boolean {
        return scheme == "https" || scheme == "http"
    }

    fun isContentScheme(scheme: String): Boolean {
        return scheme == ContentResolver.SCHEME_CONTENT
    }

    fun isFileScheme(scheme: String): Boolean {
        return scheme == ContentResolver.SCHEME_FILE
    }
}