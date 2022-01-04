package com.frolo.audiofx;

import java.io.Serializable;
import java.util.Arrays;


public final class CustomPreset implements Preset, Serializable {
    public final static long NO_ID = -1;

    private final long id;
    private final String name;
    private final short[] levels;

    public CustomPreset(long id, String name, short[] levels) {
        this.id = id;
        this.name = name != null ? name : "";
        this.levels = levels;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * USE CAREFULLY: don't call this method too many times.
     * It creates a copy of the levels array.
     * @return copy of the levels array
     */
    public short[] getLevels() {
        short[] copy = new short[levels.length];
        System.arraycopy(levels, 0, copy, 0, levels.length);
        return copy;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(levels) * name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CustomPreset)) {
            return false;
        }

        CustomPreset other = (CustomPreset) obj;

        if (!name.equalsIgnoreCase(other.name)) {
            return false;
        }

        if (levels.length != other.levels.length) {
            return false;
        }

        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != other.levels[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(":[");
        for (int i = 0; i < levels.length; i++) {
            builder.append(i).append("=").append(levels[i]);
            if (i != levels.length - 1) builder.append(", ");
        }
        builder.append("]");
        return builder.toString();
    }
}
