package com.frolo.player;


/**
 * Represents an association of keys of type {@link K} with values of type {@link V}.
 * NOTE: the association is mutable and must be thread-safe.
 * @param <K> type of keys
 * @param <V> type of values
 */
public interface Tagged<K, V> {
    V getTag(K key);

    void putTag(K key, V value);

    void removeTag(K key);
}
