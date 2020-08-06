package com.frolo.muse.model.crossfade;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public final class CrossFadeParams {

    @NotNull
    public static CrossFadeParams none() {
        return new CrossFadeParams(0, false);
    }

    @NotNull
    public static CrossFadeParams create(int interval, boolean smartInterval) {
        return new CrossFadeParams(interval, smartInterval);
    }

    private final int mInterval;
    private final boolean mSmartInterval;

    private CrossFadeParams(int interval, boolean smartInterval) {
        mInterval = interval;
        mSmartInterval = smartInterval;
    }

    public boolean isEnabled() {
        return mInterval > 0;
    }

    public int getInterval() {
        return mInterval;
    }

    public boolean isSmartInterval() {
        return mSmartInterval;
    }

    @Override
    public String toString() {
        return "CrossFadeParams{" +
                "interval=" + mInterval +
                ", smartInterval=" + mSmartInterval +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof CrossFadeParams)) return false;

        final CrossFadeParams other = (CrossFadeParams) o;
        return mInterval == other.mInterval && mSmartInterval == other.mSmartInterval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mInterval, mSmartInterval);
    }

}
