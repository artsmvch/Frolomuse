package com.frolo.audiofx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;


@Deprecated
public final class CustomPreset implements Preset, Serializable {
    public final static long NO_ID = -1;

    private static short[] copy(short[] src) {
        short[] dest = new short[src.length];
        System.arraycopy(src, 0, dest, 0, src.length);
        return dest;
    }

    @Nullable
    public static short[] getRawLevels(@NonNull CustomPreset preset) {
        if (preset.levels == null) {
            return null;
        }
        return copy(preset.levels);
    }

    private final long id;
    private final String name;
    private final short[] levels;

    public CustomPreset(long id, String name, short[] levels) {
        this.id = id;
        this.name = name != null ? name : "";
        this.levels = copy(levels);
    }

    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getLevelCount() {
        return levels.length;
    }

    public short getLevelAt(int index) {
        return levels[index];
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(levels) * name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CustomPreset) {
            return equalsImpl(this, (CustomPreset) obj);
        }
        return false;
    }

    private boolean equalsImpl(CustomPreset preset1, CustomPreset preset2) {
        if (!preset1.name.equalsIgnoreCase(preset2.name)) {
            return false;
        }
        if (preset1.levels.length != preset2.levels.length) {
            return false;
        }
        for (int i = 0; i < preset1.levels.length; i++) {
            if (preset1.levels[i] != preset2.levels[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(":[");
        for (int i = 0; i < levels.length; i++) {
            builder.append(i).append("=").append(levels[i]);
            if (i != levels.length - 1) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }
}
