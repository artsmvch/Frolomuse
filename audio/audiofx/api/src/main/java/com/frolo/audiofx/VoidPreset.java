package com.frolo.audiofx;

import java.util.Objects;


/**
 * Preset that does nothing: it doesn't apply any settings to audio fx.
 */
@Deprecated
public final class VoidPreset implements Preset {
    private final String name;

    public VoidPreset(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoidPreset that = (VoidPreset) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public String getName() {
        return name;
    }
}
