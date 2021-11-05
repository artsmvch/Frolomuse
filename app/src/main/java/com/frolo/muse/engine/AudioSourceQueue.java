package com.frolo.muse.engine;

import androidx.annotation.NonNull;

import com.frolo.muse.util.CollectionUtil;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


// Thread-safe
public final class AudioSourceQueue implements Tagged<Object, Object>, Cloneable {

    // By default
    public static final boolean UNIQUE = false;

    public interface Callback {
        void invalidate(AudioSourceQueue queue);
    }

    private final List<AudioSource> mItems;
    // If this flag is true then the queue has no item collision (i.e. represents a set of audio sources)
    private final boolean mUnique;
    // Each callback has its own executor on which the invalidation is performed
    private final Map<Callback, Executor> mCallbacks = new WeakHashMap<>();

    private final ReadWriteLock mTagsLock = new ReentrantReadWriteLock();
    private final Map<Object, Object> mTags = new HashMap<>();

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
        return new AudioSourceQueue(new ArrayList<AudioSource>(), UNIQUE);
    }

    // Factory
    public static AudioSourceQueue create(List<AudioSource> items, boolean unique) {
        return new AudioSourceQueue(items, unique);
    }

    public static AudioSourceQueue create(List<AudioSource> items) {
        return create(items, UNIQUE);
    }

    private AudioSourceQueue(List<AudioSource> items, boolean unique) {
        this.mItems = unique
                ? new ArrayList<>(new LinkedHashSet<>(items))
                : new ArrayList<>(items);
        this.mUnique = unique;
    }

    /* package */ Map<Object, Object> getTagsSnapshot() {
        final Lock lock = mTagsLock.readLock();
        try {
            lock.lock();
            return new HashMap<>(mTags);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Nullable
    public Object getTag(Object key) {
        final Lock lock = mTagsLock.readLock();
        try {
            lock.lock();
            return mTags.get(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void putTag(Object key, Object value) {
        final Lock lock = mTagsLock.writeLock();
        try {
            lock.lock();
            mTags.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeTag(Object key) {
        final Lock lock = mTagsLock.writeLock();
        try {
            lock.lock();
            mTags.remove(key);
        } finally {
            lock.unlock();
        }
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

    public synchronized boolean contains(AudioSource item) {
        return mItems.contains(item);
    }

    /* package */ synchronized void setItemAt(int position, AudioSource item) {
        mItems.set(position, item);
    }

    /* package */ synchronized void copyItemsFrom(AudioSourceQueue src) {
        mItems.clear();
        mItems.addAll(src.mItems);
        invalidateSelf();
    }

    /* package */ synchronized void addAll(Collection<?extends AudioSource> items) {
        if (mUnique) {
            mItems.removeAll(items);
            mItems.addAll(new LinkedHashSet<>(items));
        } else {
            mItems.addAll(items);
        }
        invalidateSelf();
    }

    /* package */ synchronized void addAll(int position, Collection<?extends AudioSource> items) {
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
    /* package */ synchronized void replaceAllWithSameId(AudioSource audioSource) {
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

    /* package */ synchronized void remove(AudioSource item) {
        mItems.remove(item);
        invalidateSelf();
    }

    /* package */ synchronized void removeAt(int position) {
        mItems.remove(position);
        invalidateSelf();
    }

    /* package */ synchronized void removeAll(Collection<?extends AudioSource> items) {
        mItems.removeAll(items);
        invalidateSelf();
    }

    /* package */ synchronized void clear() {
        mItems.clear();
        invalidateSelf();
    }

    /* package */ synchronized void moveItem(int fromPosition, int toPosition) {
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
    /* package */ synchronized void shuffle() {
        Collections.shuffle(mItems);
        invalidateSelf();
    }

    /**
     * Shuffles the queue and puts the given <code>putInFront</code> audio source in the front of the queue.
     * This puts the given audio source in the front only if the queue contains it.
     */
    /* package */ synchronized void shuffleWithItemInFront(AudioSource putInFront) {
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
        return createCopy();
    }

    @NonNull
    public synchronized AudioSourceQueue createCopy() {
        List<AudioSource> clonedItems = new ArrayList<>(mItems);
        Map<Object, Object> tagsSnapshot = getTagsSnapshot();
        AudioSourceQueue clonedQueue = new AudioSourceQueue(clonedItems, mUnique);
        for (Map.Entry<Object, Object> tagEntry : tagsSnapshot.entrySet()) {
            clonedQueue.putTag(tagEntry.getKey(), tagEntry.getValue());
        }
        return clonedQueue;
    }

    public synchronized List<AudioSource> getSnapshot() {
        return new ArrayList<>(mItems);
    }

    @Deprecated
    public synchronized boolean deepEquals(AudioSourceQueue other) {
        if (other == null) {
            return false;
        }

        final boolean areTagsEqual;
        final Lock tagsLock = mTagsLock.readLock();
        try {
            tagsLock.lock();
            areTagsEqual = Objects.equals(mTags, other.getTagsSnapshot());
        } finally {
            tagsLock.unlock();
        }

        final boolean areItemsEqual;
        // Double synchronized block? You wanna trap in a lock?
        synchronized (this) {
            synchronized (other) {
                areItemsEqual = CollectionUtil.areListContentsEqual(this.mItems, other.mItems);
            }
        }

        return areTagsEqual && areItemsEqual;
    }

}
