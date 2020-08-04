package com.frolo.muse.engine;

import org.jetbrains.annotations.NotNull;


/**
 * CrossFadeStrategy describes how the playback should be cross-faded.
 * {@link CrossFadeStrategy#calculateLevel(int, int)} calculates the volume level for a specific progress position of the playback.
 * There are several factory methods for creating strategies.
 */
public abstract class CrossFadeStrategy {

    public static final float MIN_LEVEL = 0f;
    public static final float MAX_LEVEL = 1f;
    public static final float NORMAL_LEVEL = 1f;

    /**
     * Gets cross-fade interval for the given <code>strategy</code>
     * @deprecated only some strategies have actual interval
     * @param strategy to get cross-fade interval from
     * @return cross-fade interval
     */
    @Deprecated
    public static int getInterval(@NotNull CrossFadeStrategy strategy) {
        if (strategy instanceof StaticIntervalCrossFade) {
            return ((StaticIntervalCrossFade) strategy).mInterval;
        }

        if (strategy instanceof SmartStaticIntervalCrossFade) {
            return ((SmartStaticIntervalCrossFade) strategy).mTargetInterval;
        }

        return 0;
    }

    @NotNull
    public static CrossFadeStrategy none() {
        return new NoneCrossFade();
    }

    @NotNull
    public static CrossFadeStrategy withStaticInterval(int interval) {
        return new StaticIntervalCrossFade(interval);
    }

    @NotNull
    public static CrossFadeStrategy withPercentInterval(float percent) {
        return new PercentIntervalCrossFade(percent);
    }

    @NotNull
    public static CrossFadeStrategy withSmartStaticInterval(int targetInterval) {
        return new SmartStaticIntervalCrossFade(targetInterval);
    }

    /**
     * This is a no-cross-fade strategy.
     */
    private static final class NoneCrossFade extends CrossFadeStrategy {

        @Override
        public float calculateLevel(int progress, int duration) {
            return NORMAL_LEVEL;
        }
    }

    private static abstract class IntervalCrossFade extends CrossFadeStrategy {

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

            return MathUtil.clamp(MIN_LEVEL, MAX_LEVEL, value);
        }
    }

    /**
     * This uses a constant interval for cross-fading.
     */
    private static final class StaticIntervalCrossFade extends IntervalCrossFade {

        final int mInterval;

        StaticIntervalCrossFade(int interval) {
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
    private static final class PercentIntervalCrossFade extends IntervalCrossFade {
        final float mPercent;

        PercentIntervalCrossFade(float percent) {
            mPercent = percent;
        }

        @Override
        int getInterval(int duration) {
            return (int) (duration * mPercent);
        }
    }

    /**
     * {@link SmartStaticIntervalCrossFade} is similar to {@link StaticIntervalCrossFade}
     * but also accounts for short audio sources.
     * If {@link SmartStaticIntervalCrossFade#mTargetInterval} is more than half the duration
     * then half the duration is used.
     * This is the best choice when you want to avoid muted playback of short audio sources.
     */
    private static final class SmartStaticIntervalCrossFade extends IntervalCrossFade {
        final int mTargetInterval;

        SmartStaticIntervalCrossFade(int targetInterval) {
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
