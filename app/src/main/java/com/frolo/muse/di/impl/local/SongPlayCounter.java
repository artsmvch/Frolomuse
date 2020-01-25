package com.frolo.muse.di.impl.local;


import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is a dispatcher center that notifies registered {@link Watcher}s
 * that play count has been changed for a song.
 *
 * A {@link Watcher} is registered by {@link SongPlayCounter#startWatching(Watcher)} method and
 * unregistered by {@link SongPlayCounter#stopWatching(Watcher)} method.
 *
 * Dispatching is done via {@link SongPlayCounter#dispatchChanged(String)} method.
 */
final class SongPlayCounter {

    interface Watcher {
        /**
         * Called when play count has been changed for a song
         * that is stored on <code>absolutePath</code>.
         * @param absolutePath of a song that play count has been changed for
         */
        void onChanged(String absolutePath);
    }

    private static final Collection<Watcher> sWatchers =
            new ConcurrentLinkedQueue<>();

    synchronized static void startWatching(Watcher w) {
        sWatchers.add(w);
    }

    synchronized static void stopWatching(Watcher w) {
        sWatchers.remove(w);
    }

    synchronized static void dispatchChanged(String absolutePath) {
        for (Watcher w : sWatchers) {
            w.onChanged(absolutePath);
        }
    }

    private SongPlayCounter() {
    }

}
