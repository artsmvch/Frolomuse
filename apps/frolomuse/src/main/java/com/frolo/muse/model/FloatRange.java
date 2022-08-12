package com.frolo.muse.model;


public final class FloatRange {
    private final float min;
    private final float max;

    public static FloatRange of(float min, float max) {
        return new FloatRange(Math.min(min, max), Math.max(min, max));
    }

    private FloatRange(float min, float max) {
        this.min = min;
        this.max = max;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!(obj instanceof FloatRange)) return false;

        FloatRange other = (FloatRange) obj;
        return min == other.min && max == other.max;
    }

    @Override
    public String toString() {
        return "FloatRange[min=" + min + ", max=" + max + "]";
    }
}
