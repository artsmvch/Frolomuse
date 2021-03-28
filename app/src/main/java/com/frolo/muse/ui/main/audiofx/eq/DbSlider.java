package com.frolo.muse.ui.main.audiofx.eq;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.R;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;


/**
 * Vertical slider with labels at the top and at the bottom.
 * The range of possible values is set using {@link DbSlider#setRange(int, int)} method.
 * The value is set using {@link DbSlider#setValue(int, boolean)} and {@link DbSlider#setValue(int)} methods.
 * Clients may observe value changes with {@link DbSlider.OnDbValueChangeListener}.
 */
final class DbSlider extends LinearLayout {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String LOG_TAG = "DbSlider";

    private static final long PROGRESS_ANIM_DURATION = 300L;
    private static final Interpolator sProgressAnimInterpolator =
            new FastOutSlowInInterpolator();

    interface OnDbValueChangeListener {
        void onDbValueChange(DbSlider slider, int value, boolean fromUser);
    }

    interface OnDbValueAnimatedListener {
        void onDbValueAnimated(DbSlider slider, int animatedValue);
    }

    private static int clamp(int min, int max, int value) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    // For internal use
    private int mSliderIndex = -1;

    // Range of possible values
    private int mMinValue = 0;
    private int mMaxValue = 1;
    // The current value
    private int mCurrValue = 0;

    // Internal widgets
    private final TextView mTopLabelTextView;
    private final TextView mBottomLabelTextView;
    private final View mVerticalSeekBarWrapper;
    private final VerticalSeekBar mVerticalSeekBar;
    private final SeekBar.OnSeekBarChangeListener mSeekBarListener =
        new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int newValue = mMinValue + progress;
                if (DEBUG) Log.d(LOG_TAG, "SeekBar value has changed to " + newValue + ", fromUser=" + fromUser);
                DbSlider.this.setValueInternal(newValue, false, fromUser, false);
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
                if (mOnValueAnimatedListener != null) {
                    mOnValueAnimatedListener.onDbValueAnimated(DbSlider.this, mMinValue + progress);
                }
            }
        };

    private OnDbValueChangeListener mListener;
    private OnDbValueAnimatedListener mOnValueAnimatedListener;

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
        // TODO: Note that the visibility of the bottom label is set to GONE in xml
        mBottomLabelTextView = findViewById(R.id.tv_bottom_label);
        mVerticalSeekBarWrapper = findViewById(R.id.vertical_seek_bar_wrapper);
        mVerticalSeekBar = findViewById(R.id.vertical_seek_bar);

        mVerticalSeekBar.setRotationAngle(VerticalSeekBar.ROTATION_ANGLE_CW_270);
        mVerticalSeekBar.setOnSeekBarChangeListener(mSeekBarListener);
    }

    public void setOnDbValueChangeListener(@Nullable OnDbValueChangeListener l) {
        this.mListener = l;
    }

    public void setOnDbValueAnimatedListener(@Nullable OnDbValueAnimatedListener l) {
        this.mOnValueAnimatedListener = l;
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
        this.mCurrValue = newValue;

        final int newRange = newMax - newMin;
        mVerticalSeekBar.setMax(newRange);

        final int targetSeekBarValue = newValue - newMin;
        mVerticalSeekBar.setProgress(targetSeekBarValue);
    }

    public int getValue() {
        return mCurrValue;
    }

    public void setValue(int value) {
        setValueInternal(value, true, false, true);
    }

    public void setValue(int value, boolean animate) {
        setValueInternal(value, animate, false, true);
    }

    /**
     * Sets the current value to the given <code>value</code>.
     * @param value new value
     * @param animate if true, then the value change should be animated
     * @param fromUser true, if this triggered by the user interaction
     * @param fromOutside true, if this triggered outside the widget
     */
    private void setValueInternal(int value, boolean animate, boolean fromUser, boolean fromOutside) {
        if (!fromUser && !fromOutside) {
            // No actions required
            return;
        }

        int newValue = clamp(mMinValue, mMaxValue, value);
        final int currValue = getValue();

        if (newValue == currValue) {
            // No actions required
            return;
        }

        if (DEBUG) Log.d(LOG_TAG, "Setting the value internal: slider_index=" + mSliderIndex + ", new_value=" + newValue);

        mCurrValue = newValue;

        // Clear the previous animation, if any
        if (mProgressAnim != null) {
            mProgressAnim.end();
            mProgressAnim = null;
        }

        dispatchDbValueChanged(newValue, fromUser);

        // Target progress value for the seek bar
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

    void setSliderIndex(int index) {
        mSliderIndex = index;
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

    void setTrackTint(@ColorInt int color) {
        ColorStateList colorStateList = ColorStateList.valueOf(color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC;

        mVerticalSeekBar.setProgressBackgroundTintList(colorStateList);
        mVerticalSeekBar.setProgressBackgroundTintMode(mode);

        mVerticalSeekBar.setProgressTintList(colorStateList);
        mVerticalSeekBar.setProgressTintMode(mode);

        mVerticalSeekBar.setSecondaryProgressTintList(colorStateList);
        mVerticalSeekBar.setSecondaryProgressTintMode(mode);
    }

    /**
     * Returns the x of the center position of the thumb relative to the bounds of this view.
     * @return the x of the center position of the thumb
     */
    int getThumbCenterX() {
        int width = mVerticalSeekBarWrapper.getMeasuredWidth()
                - mVerticalSeekBarWrapper.getPaddingLeft()
                - mVerticalSeekBarWrapper.getPaddingRight();
        return mVerticalSeekBarWrapper.getLeft() + mVerticalSeekBarWrapper.getPaddingLeft() + width / 2;
    }

    /**
     * Returns the y of the center position of the thumb relative to the bounds of this view.
     * @return the y of the center position of the thumb
     */
    int getThumbCenterY() {
        float progressPercentage = ((float) (mVerticalSeekBar.getMax() - mVerticalSeekBar.getProgress())) / mVerticalSeekBar.getMax();
        return getYForProgress(progressPercentage);
    }

    int getCenterY() {
        return getYForProgress(0.5f);
    }

    private int getYForProgress(float progressPercentage) {
        Drawable thumb = mVerticalSeekBar.getThumb();
        int thumbSize = thumb != null ? thumb.getIntrinsicWidth() : 0;
        int trackHeight = mVerticalSeekBar.getMeasuredWidth()
                - mVerticalSeekBar.getPaddingLeft()
                - mVerticalSeekBar.getPaddingRight()
                - thumbSize
                + 2 * mVerticalSeekBar.getThumbOffset();
        int progressedTrackHeight = (int) (trackHeight * progressPercentage);
        return mVerticalSeekBarWrapper.getTop()
                + mVerticalSeekBar.getTop() + mVerticalSeekBar.getPaddingTop()
                + mVerticalSeekBar.getThumbOffset() * 2
                + progressedTrackHeight;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            setRange(savedState.minValue, savedState.maxValue);
            setValue(savedState.currValue, false);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);

        savedState.minValue = mMinValue;
        savedState.maxValue = mMaxValue;
        savedState.currValue = mCurrValue;

        return savedState;
    }

    private static class SavedState extends BaseSavedState {

        int minValue;
        int maxValue;
        int currValue;

        public SavedState(Parcel source) {
            super(source);
            minValue = source.readInt();
            maxValue = source.readInt();
            currValue = source.readInt();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(minValue);
            out.writeInt(maxValue);
            out.writeInt(currValue);
        }
    }

}
