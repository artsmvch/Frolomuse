package com.frolo.muse.engine.journals

import android.util.Log
import com.frolo.player.PlayerJournal


class AndroidLogPlayerJournal(val tag: String): PlayerJournal {

    override fun logMessage(message: String?) {
        Log.d(tag, message.orEmpty())
    }

    override fun logError(message: String?, error: Throwable?) {
        if (error != null) {
            Log.e(tag, message.orEmpty(), error)
        } else {
            Log.d(tag, message.orEmpty())
        }
    }

}