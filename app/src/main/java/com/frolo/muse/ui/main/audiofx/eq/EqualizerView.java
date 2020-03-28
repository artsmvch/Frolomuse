package com.frolo.muse.ui.main.audiofx.eq;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;


public final class EqualizerView extends LinearLayout {

    private EqualizerProvider equalizerProvider;

    public EqualizerView(Context context) {
        this(context, null);
    }

    public EqualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EqualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        super.setOrientation(HORIZONTAL);
    }

    /**
     * Returns the current levels set by user.
     * @return current levels
     */
    public short[] getCurrentLevels() {
        final int numberOfBands = getChildCount();
        final short[] levels = new short[numberOfBands];
        for (int i = 0; i < numberOfBands; i++) {
            final DbSlider slider = (DbSlider) getChildAt(i);
            levels[i] = (short) slider.getValue();
        }
        return levels;
    }

    @Override
    public void onViewAdded(View child) {
        if (!(child instanceof DbSlider)) {
            throw new IllegalArgumentException("Only DbSlider can be added to EqualizerView");
        }
    }

    @Override
    public void setOrientation(int orientation) {
        throw new UnsupportedOperationException();
    }

    /**
     * Setups the view with the given <code>provider</code>.
     * @param provider equalizer provider
     */
    public void setup(@Nullable EqualizerProvider provider) {
        setup(provider, true);
    }

    /**
     * Setups the view with the given <code>provider</code>.
     * @param provider equalizer provider
     * @param animate if true, then the changes will be animated
     */
    public void setup(@Nullable EqualizerProvider provider, boolean animate) {
        this.equalizerProvider = provider;

        if (provider == null) {
            // No provider - no sliders
            removeAllViews();
            return;
        }

        final ViewGroup container = this;

        final int numberOfBands = provider.getNumberOfBands();

        final int minBandLevel = provider.getMinBandLevel();
        final int maxBandLevel = provider.getMaxBandLevel();

        int addedBandCount = 0;
        for (int bandIndex = 0; bandIndex < numberOfBands; bandIndex++) {

            final DbRange dbRange = provider.getDbRange(bandIndex);
            final int currentValue = provider.getBandLevel(bandIndex);

            final DbSlider slider;
            if (bandIndex >= container.getChildCount())  {
                DbSlider newSlider = createDbSlider();
                addView(newSlider, getChildCount());
                slider = newSlider;
            } else {
                slider = (DbSlider) container.getChildAt(bandIndex);
            }

            addedBandCount++;

            final int finalBandIndex = bandIndex;
            final DbSlider.OnDbValueChangeListener l =
                new DbSlider.OnDbValueChangeListener() {
                    @Override
                    public void onDbValueChange(DbSlider slider, int value, boolean fromUser) {
                        if (fromUser) setBandLevelInternal(finalBandIndex, value);
                    }
                };
            slider.setOnDbValueChangeListener(l);

            slider.setRange(minBandLevel, maxBandLevel);
            slider.setValue(currentValue, animate);
        }

        // removing views those weren't bounded to any band
        while (addedBandCount < container.getChildCount()) {
            container.removeViewAt(container.getChildCount() - 1);
        }
    }

    /**
     * Creates a new DbSlider and applies the right LayoutParams to it.
     * @return a new DbSlider with the right LayoutParams
     */
    private DbSlider createDbSlider() {
        final DbSlider dbSlider = new DbSlider(getContext());

        final LayoutParams lp =
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        lp.weight = 1;

        dbSlider.setLayoutParams(lp);

        return dbSlider;
    }

    private void setBandLevelInternal(int bandIndex, int level) {
        EqualizerProvider provider = equalizerProvider;
        if (provider != null) {
            final int numberOfBands = provider.getNumberOfBands();
            if (bandIndex >= 0 && bandIndex < numberOfBands) {
                provider.setBandLevel(bandIndex, (short) level);
            }
        }
    }

}
