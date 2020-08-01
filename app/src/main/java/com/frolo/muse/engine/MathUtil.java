package com.frolo.muse.engine;

import org.jetbrains.annotations.NotNull;


final class MathUtil {

    static float clamp(float min, float max, float value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    static float clamp(@NotNull Range range, float value) {
        return clamp(range.min, range.max, value);
    }

    /**
     * Represents a range of float values.
     */
    static class Range {
        final float min;
        final float max;

        Range(float min, float max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
        }
    }

    private MathUtil() {
    }

}
