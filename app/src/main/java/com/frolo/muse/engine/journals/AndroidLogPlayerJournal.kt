package com.frolo.muse.engine.journals

import android.util.Log
import com.frolo.muse.engine.PlayerJournal


class AndroidLogPlayerJournal(val tag: String): PlayerJournal {

    private val logLock = Any()

    @Synchronized
    override fun logMessage(message: String?) {
        synchronized(logLock) {
            Log.d(tag, message.orEmpty())
        }
    }

    override fun logError(message: String?, error: Throwable?) {
        synchronized(logLock) {
            if (error != null) {
                Log.e(tag, message.orEmpty(), error)
            } else {
                Log.d(tag, message.orEmpty())
            }
        }
    }

}