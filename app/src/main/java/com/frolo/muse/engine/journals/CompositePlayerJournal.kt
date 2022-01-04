package com.frolo.muse.engine.journals

import com.frolo.player.PlayerJournal


class CompositePlayerJournal(val journals: List<PlayerJournal>) : PlayerJournal {

    override fun logMessage(message: String?) {
        journals.forEach { journal ->
            journal.logMessage(message)
        }
    }

    override fun logError(message: String?, error: Throwable?) {
        journals.forEach { journal ->
            journal.logError(message, error)
        }
    }

}