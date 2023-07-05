package com.frolo.logger.impl

import com.frolo.logger.api.LogDelegate
import com.frolo.logger.api.LogLevel
import com.google.firebase.crashlytics.FirebaseCrashlytics


class FirebaseErrorLogDelegate : LogDelegate {
    override fun log(tag: String, level: LogLevel, msg: String?, e: Throwable?) {
        if (level == LogLevel.ERROR && e != null) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun log(tag: String, level: LogLevel, msg: String?) {
        if (level == LogLevel.ERROR && !msg.isNullOrBlank()) {
            FirebaseCrashlytics.getInstance().log(msg)
        }
    }
}