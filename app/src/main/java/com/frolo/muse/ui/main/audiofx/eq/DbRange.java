package com.frolo.muse.ui.main.audiofx.eq;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public final class DbRange {
    private final int min;
    private final int max;

    public DbRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) return false;

        if (!(obj instanceof DbRange)) return false;

        final DbRange other = (DbRange) obj;
        return min == other.min && max == other.max;
    }

    @NonNull
    @Override
    public String toString() {
        return "DbRange[min=" + min + ",max=" + max +"]";
    }

}
