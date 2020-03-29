package com.frolo.muse.ui.main.audiofx.eq;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frolo.muse.engine.AudioFx;
import com.frolo.muse.engine.AudioFxObserver;
import com.frolo.muse.model.preset.Preset;
import com.frolo.muse.model.reverb.Reverb;


public final class EqualizerView extends LinearLayout {

    private static final long DEBOUNCE_SET_BAND_LEVEL = 300L;

    /**
     * Special handler for delaying band level setting.
     * {@link Message#what} is used as band index.
     * {@link Message#arg1} is used as level value.
     */
    private class EqHandler extends Handler {

        EqHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final short band = (short) msg.what;
            final short level = (short) msg.arg1;

            AudioFx audioFx = EqualizerView.this.audioFx;
            if (audioFx != null) {
                final int numberOfBands = audioFx.getNumberOfBands();
                if (band >= 0 && band < numberOfBands) {
                    audioFx.setBandLevel(band, level);
                }
            }
        }
    }

    private final EqHandler handler;

    private AudioFx audioFx;

    private final AudioFxObserver audioFxObserver = new AudioFxObserver() {
        @Override public void onEnabled(AudioFx audioFx) { }

        @Override public void onDisabled(AudioFx audioFx) { }

        @Override
        public void onBandLevelChanged(AudioFx audioFx, short band, short level) {
            if (band >= 0 && band < getChildCount()) {
                DbSlider slider = (DbSlider) getChildAt(band);
                slider.setValue(level);
            }
        }

        @Override public void onPresetUsed(AudioFx audioFx, Preset preset) { }

        @Override public void onBassStrengthChanged(AudioFx audioFx, short strength) { }

        @Override public void onVirtualizerStrengthChanged(AudioFx audioFx, short strength) { }

        @Override public void onReverbUsed(AudioFx audioFx, Reverb reverb) { }
    };

    public EqualizerView(Context context) {
        this(context, null);
    }

    public EqualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EqualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        super.setOrientation(HORIZONTAL);

        handler = new EqHandler(context.getMainLooper());
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
     * Setups the view with the given <code>audioFx</code>.
     * @param audioFx to bind with
     */
    public void setup(@Nullable AudioFx audioFx) {
        setup(audioFx, true);
    }

    /**
     * Setups the view with the given <code>audioFx</code>.
     * @param audioFx to bind with
     * @param animate if true, then the changes will be animated
     */
    public void setup(@Nullable AudioFx audioFx, boolean animate) {
        AudioFx oldAudioFx = this.audioFx;
        if (oldAudioFx != null) {
            oldAudioFx.unregisterObserver(audioFxObserver);
        }

        this.audioFx = audioFx;

        if (audioFx == null) {
            // No AudioFx - no sliders
            removeAllViews();
            return;
        }

        if (isAttachedToWindow()) {
            audioFx.registerObserver(audioFxObserver);
        }

        final ViewGroup container = this;

        final int numberOfBands = audioFx.getNumberOfBands();

        final int minBandLevel = audioFx.getMinBandLevelRange();
        final int maxBandLevel = audioFx.getMaxBandLevelRange();

        int addedBandCount = 0;
        for (short bandIndex = 0; bandIndex < numberOfBands; bandIndex++) {

            //final DbRange dbRange = audioFx.getDbRange(bandIndex);
//            int[] arr = audioFx.getBandFreqRange(bandIndex.toShort())
//            int min = arr.getOrNull(0) ?: 0
//            int max = arr.getOrNull(1) ?: 0
            final int currentValue = audioFx.getBandLevel(bandIndex);

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
        handler.removeMessages(bandIndex);
        Message message = handler.obtainMessage(bandIndex, level, 0);
        handler.sendMessageDelayed(message, DEBOUNCE_SET_BAND_LEVEL);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        AudioFx currAudioFx = this.audioFx;
        if (currAudioFx != null) {
            currAudioFx.registerObserver(audioFxObserver);

            final int numberOfBands = audioFx.getNumberOfBands();
            final int viewChildCount = getChildCount();
            // Actually, numberOfBands must be equal viewChildCount
            for (short i = 0; i < Math.min(numberOfBands, viewChildCount); i++) {
                DbSlider slider = (DbSlider) getChildAt(i);
                slider.setValue(audioFx.getBandLevel(i));
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        AudioFx currAudioFx = this.audioFx;
        if (currAudioFx != null) {
            currAudioFx.unregisterObserver(audioFxObserver);
        }

        handler.removeCallbacksAndMessages(null);
    }
}
