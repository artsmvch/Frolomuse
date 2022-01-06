package com.frolo.player;

import org.jetbrains.annotations.NotNull;


final class MathUtil {

    static float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    static float clamp(float value, @NotNull Range range) {
        return clamp(value, range.min, range.max);
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
