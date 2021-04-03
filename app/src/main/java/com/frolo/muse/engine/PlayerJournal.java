package com.frolo.muse.engine;


/**
 * Simple journal for {@link Player}.
 */
public interface PlayerJournal {
    void logMessage(String message);
    void logError(String message, Throwable error);

    PlayerJournal EMPTY = new PlayerJournal() {
        @Override
        public void logMessage(String message) {
        }
        @Override
        public void logError(String message, Throwable error) {
        }
    };
}
