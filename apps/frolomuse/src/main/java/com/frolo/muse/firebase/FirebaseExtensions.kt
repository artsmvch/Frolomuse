package com.frolo.muse.firebase

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal fun withCrashlytics(block: FirebaseCrashlytics.() -> Unit) {
    kotlin.runCatching { FirebaseCrashlytics.getInstance().block() }
}