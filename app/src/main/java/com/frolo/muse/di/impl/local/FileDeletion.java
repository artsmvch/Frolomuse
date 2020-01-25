package com.frolo.muse.di.impl.local;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Dispatcher center:
 * the clients may listen to a file deletion via {@link FileDeletion#startWatching(Watcher)}
 * and stop listening via {@link FileDeletion#stopWatching(Watcher)}.
 *
 * Dispatching is done via {@link FileDeletion#dispatchDeleted(List)} method.
 */
final class FileDeletion {

    interface Watcher {
        void onFilesDeleted(List<File> files);
    }

    private static final Collection<Watcher> sWatchers =
            new ConcurrentLinkedQueue<>();

    static void startWatching(Watcher w) {
        sWatchers.add(w);
    }

    static void stopWatching(Watcher w) {
        sWatchers.remove(w);
    }

    static void dispatchDeleted(List<File> files) {
        for (Watcher w : sWatchers) {
            w.onFilesDeleted(files);
        }
    }

    private FileDeletion() {
    }
}
