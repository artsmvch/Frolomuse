package com.frolo.muse.ui.main.audiofx.eq;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.R;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;


/**
 * Vertical slider with labels at the top and at the bottom.
 * The range of possible values is set using {@link DbSlider#setRange(int, int)} method.
 * The value is set using {@link DbSlider#setValue(int, boolean)} and {@link DbSlider#setValue(int)} methods.
 * Clients may observe value changes with {@link DbSlider.OnDbValueChangeListener}.
 */
public final class DbSlider extends LinearLayout {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final long PROGRESS_ANIM_DURATION = 150L;
    private static final Interpolator sProgressAnimInterpolator =
            new FastOutLinearInInterpolator();

    interface OnDbValueChangeListener {
        void onDbValueChange(DbSlider slider, int value, boolean fromUser);
    }

    private static int clamp(int min, int max, int value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private int mMinValue = 0;
    private int mMaxValue = 1;

    // Inner views
    private final TextView mTopLabelTextView;
    private final TextView mBottomLabelTextView;
    private final VerticalSeekBar mVerticalSeekBar;
    private final SeekBar.OnSeekBarChangeListener mSeekBarListener =
        new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newValue = mMinValue + progress;
                DbSlider.this.setValueInternal(newValue, false, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        };

    // Animation
    private Animator mProgressAnim;
    private final ValueAnimator.AnimatorUpdateListener mProgressAnimUpdateListener =
        new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int progress = (int) animation.getAnimatedValue();
                mVerticalSeekBar.setProgress(progress);
            }
        };

    private OnDbValueChangeListener mListener;

    public DbSlider(Context context) {
        this(context, null);
    }

    public DbSlider(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DbSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        super.setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);

        inflate(context, R.layout.include_db_slider_content, this);

        mTopLabelTextView = findViewById(R.id.tv_top_label);
        mBottomLabelTextView = findViewById(R.id.tv_bottom_label);
        mVerticalSeekBar = findViewById(R.id.vertical_seek_bar);

        mVerticalSeekBar.setRotationAngle(VerticalSeekBar.ROTATION_ANGLE_CW_270);
        mVerticalSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
    }

    public void setOnDbValueChangeListener(@Nullable OnDbValueChangeListener l) {
        this.mListener = l;
    }

    public void setRange(int min, int max) {
        if (max < min && DEBUG) {
            throw new IllegalArgumentException("max < min: max=" + max + ", min=" + min);
        }

        final int currValue = getValue();

        final int newMin = min;
        final int newMax = max > min ? max : min + 1;
        final int newValue = clamp(newMin, newMax, currValue);

        this.mMinValue = newMin;
        this.mMaxValue = newMax;

        final int newRange = newMax - newMin;
        mVerticalSeekBar.setMax(newRange);

        setValueInternal(newValue, false, false);
    }

    public int getValue() {
        return mVerticalSeekBar.getProgress() + mMinValue;
    }

    public void setValue(int value) {
        setValueInternal(value, true, false);
    }

    public void setValue(int value, boolean animate) {
        setValueInternal(value, animate, false);
    }

    private void setValueInternal(int value, boolean animate, boolean fromUser) {
        int newValue = clamp(mMinValue, mMaxValue, value);
        final int currValue = getValue();
        if (newValue == currValue) {
            // No actions required
            return;
        }

        // Clear the previous animation, if any
        if (mProgressAnim != null) {
            mProgressAnim.end();
            mProgressAnim = null;
        }

        dispatchDbValueChanged(newValue, fromUser);

        // target progress value for the seek bar
        final int seekBarTargetProgress = newValue - mMinValue;

        if (animate) {
            final ValueAnimator newAnim =
                    ValueAnimator.ofInt(mVerticalSeekBar.getProgress(), seekBarTargetProgress);
            newAnim.setDuration(PROGRESS_ANIM_DURATION);
            newAnim.setInterpolator(sProgressAnimInterpolator);
            newAnim.addUpdateListener(mProgressAnimUpdateListener);
            newAnim.start();

            mProgressAnim = newAnim;
        } else {
            mVerticalSeekBar.setProgress(seekBarTargetProgress);
        }
    }

    public void setTopLabel(@Nullable String label) {
        mTopLabelTextView.setText(label);
    }

    public void setBottomLabel(@Nullable String label) {
        mBottomLabelTextView.setText(label);
    }

    private void dispatchDbValueChanged(int newValue, boolean fromUser) {
        final OnDbValueChangeListener l = mListener;
        if (l != null) {
            l.onDbValueChange(this, newValue, fromUser);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mProgressAnim != null) {
            mProgressAnim.end();
            mProgressAnim = null;
        }
    }

    @Override
    public void setOrientation(int orientation) {
        throw new UnsupportedOperationException();
    }
}
