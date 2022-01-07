package com.frolo.music.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Comparator;


// Simply does the basic comparison of nullable objects and then
// delegates the comparison of non-null objects to the inheritor.
/* package-private */ abstract class BaseComparator<T> implements Comparator<T> {
    @Override
    public final int compare(@Nullable T item1, @Nullable T item2) {
        if (item1 == null && item2 == null) {
            return 0;
        }
        if (item1 == null) {
            return -1;
        }
        if (item2 == null) {
            return 1;
        }
        return safelyCompare(item1, item2);
    }

    abstract int safelyCompare(@NonNull T item1, @NonNull T item2);
}
