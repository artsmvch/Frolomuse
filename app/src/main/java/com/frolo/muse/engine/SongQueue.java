package com.frolo.muse.engine;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.frolo.muse.model.media.Song;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;


// Thread-safe
public final class SongQueue implements Cloneable {

    // By default
    public static final boolean UNIQUE = false;

    // Types
    public static final int NONE = -1;
    public static final int SINGLE = 0;
    public static final int ALBUM = 1;
    public static final int ARTIST = 2;
    public static final int GENRE = 3;
    public static final int PLAYLIST = 4;
    public static final int FOLDER = 5;
    public static final int CHUNK = 6;
    public static final int FAVOURITES = 7;

    // No id
    public static final long NO_ID = -1;

    @IntDef({NONE, SINGLE, ALBUM, ARTIST, GENRE, PLAYLIST, FOLDER, CHUNK, FAVOURITES})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.LOCAL_VARIABLE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    public @interface QueueType { }

    public interface Callback {
        void invalidate(SongQueue queue);
    }

    @QueueType
    private final int mType;
    private final long mId;
    private final String mName;
    private final List<Song> mSongs;
    // If this flag is true then the queue has no song collision (i.e. represents a set of songs)
    private final boolean mUnique;
    // Each callback has its own executor on which the invalidation is performed
    private final Map<Callback, Executor> mCallbacks = new WeakHashMap<>();

    private class Invalidator implements Runnable {
        final Callback mCallback;

        Invalidator(Callback c) {
            this.mCallback = c;
        }

        @Override
        public void run() {
            mCallback.invalidate(SongQueue.this);
        }
    }

    // Factory
    public static SongQueue empty() {
        return new SongQueue(NONE, NO_ID, "", new ArrayList<Song>(), UNIQUE);
    }

    // Factory
    public static SongQueue create(
            @QueueType int type,
            long id,
            String name,
            List<Song> songs,
            boolean unique) {
        return new SongQueue(type, id, name, songs, unique);
    }

    public static SongQueue create(
            @QueueType int type,
            long id,
            String name,
            List<Song> songs) {
        return create(type, id, name, songs, UNIQUE);
    }

    private SongQueue(int type, long id, String name, List<Song> songs, boolean unique) {
        this.mType = type;
        this.mId = id;
        this.mName = name;
        this.mSongs = unique
                ? new ArrayList<>(new LinkedHashSet<>(songs))
                : new ArrayList<>(songs);
        this.mUnique = unique;
    }

    /**
     * Registers new callback observer.
     * @param callback to register
     * @param executor in which thread the callback will be called
     */
    public void registerCallback(Callback callback, Executor executor) {
        synchronized (mCallbacks) {
            mCallbacks.put(callback, executor);
        }
    }

    /**
     * Unregisters previously registered callback.
     * This method has no effect if there is no such callback.
     * @param callback to unregister
     */
    public void unregisterCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    @QueueType
    public int getType() {
        return mType;
    }

    public long getId() {
        return mId;
    }

    public boolean isUnique() {
        return mUnique;
    }

    public synchronized boolean isEmpty() {
        return mSongs.isEmpty();
    }

    public synchronized int getLength() {
        return mSongs.size();
    }

    public synchronized int indexOf(Song song) {
        return mSongs.indexOf(song);
    }

    public synchronized Song getItemAt(int position) {
        return mSongs.get(position);
    }

    public synchronized void setItemAt(int position, Song song) {
        mSongs.set(position, song);
    }

    public synchronized boolean contains(Song song) {
        return mSongs.contains(song);
    }

    /*package*/ synchronized void copyItemsFrom(SongQueue src) {
        mSongs.clear();
        mSongs.addAll(src.mSongs);
        invalidateSelf();
    }

    /*package*/ synchronized void addAll(Collection<?extends Song> items) {
        if (mUnique) {
            mSongs.removeAll(items);
            mSongs.addAll(items);
        } else {
            mSongs.addAll(items);
        }
        invalidateSelf();
    }

    /*package*/ synchronized void addAll(int position, Collection<?extends Song> items) {
        if (mUnique) {
            mSongs.removeAll(items);

            if (position >= 0 && position < mSongs.size()) {
                mSongs.addAll(position, items);
            } else {
                mSongs.addAll(items);
            }
        } else {
            if (position >= 0 && position < mSongs.size()) {
                mSongs.addAll(position, items);
            } else {
                mSongs.addAll(items);
            }
        }
        invalidateSelf();
    }

    /*package*/ synchronized void remove(Song song) {
        mSongs.remove(song);
        invalidateSelf();
    }

    /*package*/ synchronized void removeAt(int position) {
        mSongs.remove(position);
        invalidateSelf();
    }

    /*package*/ synchronized void removeAll(Collection<?extends Song> items) {
        mSongs.removeAll(items);
        invalidateSelf();
    }

    /*package*/ synchronized void clear() {
        mSongs.clear();
        invalidateSelf();
    }

    /*package*/ synchronized void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mSongs, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mSongs, i, i - 1);
            }
        }
        invalidateSelf();
    }

    /**
     * Shuffles the queue.
     */
    /*package*/ synchronized void shuffle() {
        Collections.shuffle(mSongs);
        invalidateSelf();
    }

    /**
     * Shuffles the queue and puts the given <code>putInFront</code> song in the front of the queue.
     * This puts the given song in the front only if the queue contains it.
     */
    /*package*/ synchronized void shuffleWithSongInFront(Song putInFront) {
        Collections.shuffle(mSongs);
        if (mSongs.remove(putInFront)) {
            mSongs.add(0, putInFront);
        }
        invalidateSelf();
    }

    private void invalidateSelf() {
        synchronized (mCallbacks) {
            for (Map.Entry<Callback, Executor> entry : mCallbacks.entrySet()) {
                Runnable r = new Invalidator(entry.getKey());
                Executor executor = entry.getValue();
                executor.execute(r);
            }
        }
    }

    @NonNull
    @Override
    protected synchronized SongQueue clone() {
        List<Song> clonedItems = new ArrayList<>(mSongs);
        return new SongQueue(mType, mId, mName, clonedItems, mUnique);
    }

    public synchronized List<Song> getSnapshot() {
        return new ArrayList<>(mSongs);
    }

}
