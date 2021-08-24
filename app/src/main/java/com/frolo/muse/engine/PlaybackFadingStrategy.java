package com.frolo.muse.engine;

import org.jetbrains.annotations.NotNull;


/**
 * PlaybackFadingStrategy describes how the playback should fade in and out.
 * {@link PlaybackFadingStrategy#calculateLevel(int, int)} calculates the volume level for a specific progress position of the playback.
 * There are several factory methods for creating strategies.
 */
public abstract class PlaybackFadingStrategy {

    public static final float MIN_LEVEL = 0f;
    public static final float MAX_LEVEL = 1f;
    public static final float NORMAL_LEVEL = 1f;

    /**
     * Gets fading interval for the given <code>strategy</code>
     * @deprecated only some strategies have actual interval
     * @param strategy to get fading interval from
     * @return fading interval
     */
    @Deprecated
    public static int getInterval(@NotNull PlaybackFadingStrategy strategy) {
        if (strategy instanceof StaticIntervalPlaybackFading) {
            return ((StaticIntervalPlaybackFading) strategy).mInterval;
        }

        if (strategy instanceof SmartStaticIntervalPlaybackFading) {
            return ((SmartStaticIntervalPlaybackFading) strategy).mTargetInterval;
        }

        return 0;
    }

    @NotNull
    public static PlaybackFadingStrategy none() {
        return new NonePlaybackFading();
    }

    @NotNull
    public static PlaybackFadingStrategy withStaticInterval(int interval) {
        return new StaticIntervalPlaybackFading(interval);
    }

    @NotNull
    public static PlaybackFadingStrategy withPercentInterval(float percent) {
        return new PercentIntervalPlaybackFading(percent);
    }

    @NotNull
    public static PlaybackFadingStrategy withSmartStaticInterval(int targetInterval) {
        return new SmartStaticIntervalPlaybackFading(targetInterval);
    }

    /**
     * This is a no-fading strategy.
     */
    private static final class NonePlaybackFading extends PlaybackFadingStrategy {

        @Override
        public float calculateLevel(int progress, int duration) {
            return NORMAL_LEVEL;
        }
    }

    private static abstract class IntervalPlaybackFading extends PlaybackFadingStrategy {

        abstract int getInterval(int duration);

        @Override
        public final float calculateLevel(int progress, int duration) {
            final float interval = (float) getInterval(duration);
            final float value;

            if (progress < interval) {
                value = progress / interval;
            } else if (progress > duration - interval) {
                value = (duration - progress) / interval;
            } else {
                // Default level
                value = NORMAL_LEVEL;
            }

            return MathUtil.clamp(value, MIN_LEVEL, MAX_LEVEL);
        }
    }

    /**
     * This uses a constant interval for fading.
     */
    private static final class StaticIntervalPlaybackFading extends IntervalPlaybackFading {

        final int mInterval;

        StaticIntervalPlaybackFading(int interval) {
            mInterval = interval;
        }

        @Override
        int getInterval(int duration) {
            return mInterval;
        }
    }

    /**
     * For this strategy, the interval is calculated by multiplying the duration by a percent.
     */
    private static final class PercentIntervalPlaybackFading extends IntervalPlaybackFading {
        final float mPercent;

        PercentIntervalPlaybackFading(float percent) {
            mPercent = percent;
        }

        @Override
        int getInterval(int duration) {
            return (int) (duration * mPercent);
        }
    }

    /**
     * {@link SmartStaticIntervalPlaybackFading} is similar to {@link StaticIntervalPlaybackFading}
     * but also accounts for short audio sources.
     * If {@link SmartStaticIntervalPlaybackFading#mTargetInterval} is more than half the duration
     * then half the duration is used.
     * This is the best choice when you want to avoid muted playback of short audio sources.
     */
    private static final class SmartStaticIntervalPlaybackFading extends IntervalPlaybackFading {
        final int mTargetInterval;

        SmartStaticIntervalPlaybackFading(int targetInterval) {
            mTargetInterval = targetInterval;
        }

        @Override
        int getInterval(int duration) {
            return Math.min(mTargetInterval, duration / 2);
        }
    }

    /**
     * Calculates the volume level for the given <code>progress</code> knowing that the duration is <code>duration</code>.
     * The return value should be in the range 0f..1f.
     * 0f means that the playback is muted and 1f means normal volume.
     * @param progress to calculate level for
     * @param duration duration of the audio source
     * @return volume level
     */
    public abstract float calculateLevel(int progress, int duration);

}
