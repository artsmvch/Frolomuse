package com.frolo.muse.model;


public final class ShortRange {
    private final short min;
    private final short max;

    public static ShortRange of(short min, short max) {
        return new ShortRange((short) Math.min(min, max), (short) Math.max(min, max));
    }

    private ShortRange(short min, short max) {
        this.min = min;
        this.max = max;
    }

    public short getMin() {
        return min;
    }

    public short getMax() {
        return max;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (!(obj instanceof ShortRange)) return false;

        ShortRange other = (ShortRange) obj;
        return min == other.min && max == other.max;
    }

    @Override
    public String toString() {
        return "ShortRange[min=" + min + ", max=" + max + "]";
    }
}
