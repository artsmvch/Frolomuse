package com.frolo.player;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;


final class DefaultShuffler<T> implements Shuffler<T> {
    @Override
    public void shuffle(@NonNull List<T> list) {
        Collections.shuffle(list);
    }
}
