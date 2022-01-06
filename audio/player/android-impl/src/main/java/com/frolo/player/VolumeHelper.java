package com.frolo.player;


final class VolumeHelper {

    private final static MathUtil.Range sVolumeRange = new MathUtil.Range(0.0f, 1.0f);

    /**
     * Computes the volume for the given <code>level</code>.
     * @param level to compute volume for
     * @return volume
     */
    public static float computeVolume(float level) {
        final float volume = doMagicTransformation(level);
        return MathUtil.clamp(volume, sVolumeRange);
    }

    private static float doCubicTransformation(float level) {
        // The min volume that the user can hear from MediaPlayer
        final float lowerAsymptote = 0.01f;
        return lowerAsymptote + (float) Math.pow(level, 3) * (1f - lowerAsymptote);
    }

    // The idea was taken from https://android.googlesource.com/platform/packages/apps/DeskClock/+/master/src/com/android/deskclock/AsyncRingtonePlayer.java.
    private static float doMagicTransformation(float level) {
        final float fractionComplete = level;
        // Use the fraction to compute a target decibel between -40dB (near silent) and 0dB (max).
        final float gain = (fractionComplete * 40) - 40;
        // Convert the target gain (in decibels) into the corresponding volume scalar.
        return (float) Math.pow(10f, gain / 20f);
    }

    private VolumeHelper() {
    }

}
