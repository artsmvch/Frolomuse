package com.frolo.muse.util;


import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

// Map that holds just 1 pair of value-key
final class SinglePairMap<K, V> implements Map<K, V>, Serializable {
    private final K first;
    private final V second;

    SinglePairMap(K first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return first == key || (first != null && key != null && first.equals(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return second == value || (second != null && value != null && second.equals(value));
    }

    @Override
    public V get(Object key) {
        if (containsKey(key))
            return second;
        return null;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public Set<K> keySet() {
        return new SingleItemCollection<>(first);
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return new SingleItemCollection<>(second);
    }

    @NonNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        Entry<K, V> entry = new Entry<K, V>() {
            @Override public K getKey() {
                return first;
            }
            @Override public V getValue() {
                return second;
            }
            @Override public V setValue(V value) {
                throw new UnsupportedOperationException();
            }
        };
        return new SingleItemCollection<>(entry);
    }
}
