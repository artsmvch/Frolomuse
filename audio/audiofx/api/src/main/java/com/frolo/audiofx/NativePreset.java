package com.frolo.audiofx;

import java.io.Serializable;
import java.util.Objects;


public final class NativePreset implements Preset, Serializable {
    public static final short NO_INDEX = -1;

    private final String name;
    private final short index;

    public NativePreset(short index, String name) {
        this.name = name != null ? name : "";
        this.index = index;
    }

    @Override
    public String getName() {
        return name;
    }

    public short getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof NativePreset) {
            NativePreset another = (NativePreset) obj;
            return index == another.index && name.equals(another.name);
        } else return false;
    }

    @Override
    public String toString() {
        return "name=" + name + ", index=" + index;
    }
}