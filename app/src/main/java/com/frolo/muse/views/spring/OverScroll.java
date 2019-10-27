package com.frolo.muse.views.spring;


class OverScroll {

    private static final float OVERSCROLL_DAMP_FACTOR = 0.07f;

    /**
     * This curve determines how the effect of scrolling over the limits of the page diminishes
     * as the user pulls further and further from the bounds
     *
     * @param f The percentage of how much the user has overscrolled.
     * @return A transformed percentage based on the influence curve.
     */
    private static float overScrollInfluenceCurve(float f) {
        f -= 1.0f;
        return f * f * f + 1.0f;
    }

    /**
     * @param amount The original amount overscrolled.
     * @param max The maximum amount that the View can overscroll.
     * @return The dampened overscroll amount.
     */
    public static int dampedScroll(float amount, int max) {
        if (Float.compare(amount, 0) == 0) return 0;

        float f = amount / max;
        f = f / (Math.abs(f)) * (overScrollInfluenceCurve(Math.abs(f)));

        // Clamp this factor, f, to -1 < f < 1
        if (Math.abs(f) >= 1) {
            f /= Math.abs(f);
        }

        return Math.round(OVERSCROLL_DAMP_FACTOR * f * max);
    }
}
