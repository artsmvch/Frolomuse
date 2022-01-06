package com.frolo.player;


public interface Tagged<K, V> {
    V getTag(K key);

    void putTag(K key, V value);

    void removeTag(K key);
}
