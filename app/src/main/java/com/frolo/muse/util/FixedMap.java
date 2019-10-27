package com.frolo.muse.util;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FixedMap<K, V> implements Map<K, V> {
    private static class FixedEntry<K, V> implements Entry<K, V> {
        K key;
        V value;
        FixedEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public K getKey() {
            return key;
        }
        @Override
        public V getValue() {
            return value;
        }
        @Override
        public V setValue(V v) {
            throw new UnsupportedOperationException("This entry is fixed");
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FixedEntry) {
                // comparing the keys
                return Objects.equals(key, ((FixedEntry) obj).key);
            }
            return false;
        }
    }

    public static class Builder<K, V> {
        private final Set<FixedEntry<K, V>> mEntries;

        // if we specify right initial capacity then it may help improve performance
        public Builder(int initialCapacity) {
            this.mEntries = new HashSet<>(initialCapacity);
        }

        public Builder() {
            this(5);
        }

        public Builder<K, V> put(K key, V value) {
            // try finding entry with the giving key
            for (FixedEntry<K, V> entry : mEntries) {
                Object entryKey = entry.key;
                if (Objects.equals(entryKey, key)) {
                    // ok, such key exists, replacing the old value with a new one
                    entry.value = value;
                    return this;
                }
            }
            // no such key found, creating and adding a new entry
            mEntries.add(new FixedEntry<>(key, value));
            return this;
        }

        public FixedMap<K, V> build() {
            return new FixedMap<>(mEntries);
        }
    }

    private final Set<FixedEntry<K, V>> mEntries;

    private final Object mEntryLock = new Object();
    private volatile Set<Entry<K, V>> mEntrySet;

    private final Object mKeySetLock = new Object();
    private volatile Set<K> mKeySet;

    private final Object mValueSetLock = new Object();
    private volatile Set<V> mValueSet;

    private FixedMap(Set<FixedEntry<K, V>> entries) {
        this.mEntries = entries;
    }

    @Override
    public int size() {
        return mEntries.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object o) {
        for (Entry entry : mEntries) {
            Object key = entry.getKey();
            if (Objects.equals(key, 0)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        for (Entry entry : mEntries) {
            Object value = entry.getValue();
            if (Objects.equals(value, 0)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(@Nullable Object o) {
        for (Entry<K, V> entry : mEntries) {
            Object key = entry.getKey();
            if (Objects.equals(key, 0)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K k, V v) {
        throw new UnsupportedOperationException("This map is fixed");
    }

    @Override
    public V remove(Object o) {
        throw new UnsupportedOperationException("This map is fixed");
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException("This map is fixed");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This map is fixed");
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        if (mKeySet == null) {
            synchronized (mKeySetLock) {
                if (mKeySet == null) {
                    mKeySet = new HashSet<>(mEntries.size());
                    for (Entry<K, ?> entry : mEntries) {
                        mKeySet.add(entry.getKey());
                    }
                }
            }
        }
        return mKeySet;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        if (mValueSet == null) {
            synchronized (mValueSetLock) {
                if (mValueSet == null) {
                    mValueSet = new HashSet<>(mEntries.size());
                    for (Entry<?, V> entry : mEntries) {
                        mValueSet.add(entry.getValue());
                    }
                }
            }
        }
        return mValueSet;
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        if (mEntrySet == null) {
            synchronized (mEntryLock) {
                if (mEntrySet == null) {
                    mEntrySet = new HashSet<>(mEntries.size());
                    for (Entry<K, V> entry : mEntries) {
                        mEntrySet.add(new FixedEntry<>(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        return mEntrySet;
    }
}
