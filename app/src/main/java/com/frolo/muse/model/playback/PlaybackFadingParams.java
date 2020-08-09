package com.frolo.muse.model.playback;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public final class PlaybackFadingParams {

    @NotNull
    public static PlaybackFadingParams none() {
        return new PlaybackFadingParams(0, false);
    }

    @NotNull
    public static PlaybackFadingParams create(int interval, boolean smartInterval) {
        return new PlaybackFadingParams(interval, smartInterval);
    }

    private final int mInterval;
    private final boolean mSmartInterval;

    private PlaybackFadingParams(int interval, boolean smartInterval) {
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
        return "PlaybackFadingParams{" +
                "interval=" + mInterval +
                ", smartInterval=" + mSmartInterval +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof PlaybackFadingParams)) return false;

        final PlaybackFadingParams other = (PlaybackFadingParams) o;
        return mInterval == other.mInterval && mSmartInterval == other.mSmartInterval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mInterval, mSmartInterval);
    }

}
