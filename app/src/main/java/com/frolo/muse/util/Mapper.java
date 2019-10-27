package com.frolo.muse.util;

import java.util.Map;

public final class Mapper {
    private Mapper() { }

    public static <K, V> Map<K, V> map(K key, V value) {
        return new SinglePairMap<>(key, value);
    }
}
