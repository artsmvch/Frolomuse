package com.frolo.muse.engine;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

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
public final class AudioSourceQueue implements Cloneable {

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
        void invalidate(AudioSourceQueue queue);
    }

    @QueueType
    private final int mType;
    private final long mId;
    private final String mName;
    private final List<AudioSource> mItems;
    // If this flag is true then the queue has no item collision (i.e. represents a set of audio sources)
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
            mCallback.invalidate(AudioSourceQueue.this);
        }
    }

    // Factory
    public static AudioSourceQueue empty() {
        return new AudioSourceQueue(NONE, NO_ID, "", new ArrayList<AudioSource>(), UNIQUE);
    }

    // Factory
    public static AudioSourceQueue create(
            @QueueType int type, long id, String name, List<AudioSource> items, boolean unique) {
        return new AudioSourceQueue(type, id, name, items, unique);
    }

    public static AudioSourceQueue create(
            @QueueType int type, long id, String name,List<AudioSource> items) {
        return create(type, id, name, items, UNIQUE);
    }

    private AudioSourceQueue(int type, long id, String name, List<AudioSource> items, boolean unique) {
        this.mType = type;
        this.mId = id;
        this.mName = name;
        this.mItems = unique
                ? new ArrayList<>(new LinkedHashSet<>(items))
                : new ArrayList<>(items);
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

    public String getName() {
        return mName;
    }

    public boolean isUnique() {
        return mUnique;
    }

    public synchronized boolean isEmpty() {
        return mItems.isEmpty();
    }

    public synchronized int getLength() {
        return mItems.size();
    }

    public synchronized int indexOf(AudioSource item) {
        return mItems.indexOf(item);
    }

    public synchronized AudioSource getItemAt(int position) {
        return mItems.get(position);
    }

    public synchronized void setItemAt(int position, AudioSource item) {
        mItems.set(position, item);
    }

    public synchronized boolean contains(AudioSource item) {
        return mItems.contains(item);
    }

    /*package*/ synchronized void copyItemsFrom(AudioSourceQueue src) {
        mItems.clear();
        mItems.addAll(src.mItems);
        invalidateSelf();
    }

    /*package*/ synchronized void addAll(Collection<?extends AudioSource> items) {
        if (mUnique) {
            mItems.removeAll(items);
            mItems.addAll(items);
        } else {
            mItems.addAll(items);
        }
        invalidateSelf();
    }

    /*package*/ synchronized void addAll(int position, Collection<?extends AudioSource> items) {
        if (mUnique) {
            mItems.removeAll(items);

            if (position >= 0 && position < mItems.size()) {
                mItems.addAll(position, items);
            } else {
                mItems.addAll(items);
            }
        } else {
            if (position >= 0 && position < mItems.size()) {
                mItems.addAll(position, items);
            } else {
                mItems.addAll(items);
            }
        }
        invalidateSelf();
    }

    /**
     * Replaces all audio sources with the same ID as the given <code>audioSource</code> with this <code>audioSource</code>.
     * @param audioSource to replace with
     */
    /*package*/ synchronized void replaceAllWithSameId(AudioSource audioSource) {
        if (audioSource == null) return;

        boolean atLeastOneReplaced = false;
        for (int i = 0; i < mItems.size(); i++) {
            final AudioSource item = mItems.get(i);
            if (item.getId() == audioSource.getId()) {
                // This item has the same ID
                mItems.set(i, AudioSources.copyAudioSource(audioSource));
                atLeastOneReplaced = true;
            }
        }
        if (atLeastOneReplaced) {
            invalidateSelf();
        }
    }

    /*package*/ synchronized void remove(AudioSource item) {
        mItems.remove(item);
        invalidateSelf();
    }

    /*package*/ synchronized void removeAt(int position) {
        mItems.remove(position);
        invalidateSelf();
    }

    /*package*/ synchronized void removeAll(Collection<?extends AudioSource> items) {
        mItems.removeAll(items);
        invalidateSelf();
    }

    /*package*/ synchronized void clear() {
        mItems.clear();
        invalidateSelf();
    }

    /*package*/ synchronized void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mItems, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mItems, i, i - 1);
            }
        }
        invalidateSelf();
    }

    /**
     * Shuffles the queue.
     */
    /*package*/ synchronized void shuffle() {
        Collections.shuffle(mItems);
        invalidateSelf();
    }

    /**
     * Shuffles the queue and puts the given <code>putInFront</code> audio source in the front of the queue.
     * This puts the given audio source in the front only if the queue contains it.
     */
    /*package*/ synchronized void shuffleWithItemInFront(AudioSource putInFront) {
        Collections.shuffle(mItems);
        if (mItems.remove(putInFront)) {
            mItems.add(0, putInFront);
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
    protected synchronized AudioSourceQueue clone() {
        List<AudioSource> clonedItems = new ArrayList<>(mItems);
        return new AudioSourceQueue(mType, mId, mName, clonedItems, mUnique);
    }

    public synchronized List<AudioSource> getSnapshot() {
        return new ArrayList<>(mItems);
    }

}
