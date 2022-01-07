package com.frolo.player;

import androidx.annotation.NonNull;

import java.util.List;


public interface Shuffler<T> {
    /**
     * Shuffles the given <code>list</code>. Unleash your imagination
     * to shuffle lists in the most sophisticated way :)
     * @param list to shuffle
     */
    void shuffle(@NonNull List<T> list);
}
