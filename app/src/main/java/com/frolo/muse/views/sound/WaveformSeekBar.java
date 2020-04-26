package com.frolo.muse.views.sound;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Interpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.frolo.muse.BuildConfig;
import com.frolo.muse.R;


/**
 * A WaveformSeekBar is for the same as {@link android.widget.SeekBar}.
 * The only difference is that the WaveformSeekBar displays a waveform represented by an array of ints.
 * That array of ints is set via {@link WaveformSeekBar#setWaveform(int[], boolean)}
 * and {@link WaveformSeekBar#setWaveform(int[], boolean)} methods.
 * The progress is set in percentage via {@link WaveformSeekBar#setProgressInPercentage(float)} method.
 * The tracking of the progress is listened using {@link WaveformSeekBar.OnSeekBarChangeListener}.
 */
public class WaveformSeekBar extends View {

    private static final String LOG_TAG = WaveformSeekBar.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final int MAX_WIDTH_NO_LIMIT = -1;

    private static final int ANIM_DURATION = 200;

    public enum WaveCornerType { NONE, AUTO, EXACTLY }

    private static float dpToPx(Context context, float dp){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private static float clampPercentage(float percent) {
        if (percent < 0) return 0f;
        if (percent > 1) return 1;
        return percent;
    }

    //Styling
    @ColorInt
    private int mWaveBackgroundColor;
    @ColorInt
    private int mWaveProgressColor;
    private float mPrefWaveGap;
    private int mWaveMaxWidth = MAX_WIDTH_NO_LIMIT; // if -1 then there is no limit for wave width
    private float mWaveWidth;
    private float mWaveGap;
    private final WaveCornerType mWaveCornerType;
    private float mWaveCornerRadius;
    private final Paint mWaveBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mWaveProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    //Animation
    private Animator mWaveAnim = null;
    private final long mWaveAnimDur;
    private final Interpolator mWaveAnimInterpolator = new FastOutLinearInInterpolator();
    private float mWaveHeightFactor = 1f;
    private final ValueAnimator.AnimatorUpdateListener mWaveAnimUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mWaveHeightFactor = (float) animation.getAnimatedValue();
                    invalidate();
                }
            };

    //Motion events
    private boolean mIsTracking = false;

    //Waveform data and its progress
    private Waveform mWaveform;
    private float mProgressPercentPosition = 0.0f;
    private float mProgressPosition = -1; // position of the progress in percentage

    //Listener
    private OnSeekBarChangeListener mListener;

    public WaveformSeekBar(Context context) {
        this(context, null);
    }

    public WaveformSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.waveformSeekBarStyle);
    }

    public WaveformSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.Base_AppTheme_WaveformSeekBar);
    }

    public WaveformSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.WaveformSeekBar, defStyleAttr, defStyleRes);
        mWaveBackgroundColor = a.getColor(R.styleable.WaveformSeekBar_waveBackgroundColor, Color.LTGRAY);
        mWaveProgressColor = a.getColor(R.styleable.WaveformSeekBar_waveProgressColor, Color.GRAY);
        mPrefWaveGap = a.getDimension(R.styleable.WaveformSeekBar_waveGap, dpToPx(context, 0f));
        mWaveMaxWidth = (int) a.getDimension(R.styleable.WaveformSeekBar_waveMaxWidth, MAX_WIDTH_NO_LIMIT);
        mWaveCornerRadius = a.getDimension(R.styleable.WaveformSeekBar_waveCornerRadius, dpToPx(context, 0f));
        mWaveCornerType = WaveCornerType.AUTO;
        mWaveAnimDur = a.getInt(R.styleable.WaveformSeekBar_waveAnimDuration, ANIM_DURATION);
        a.recycle();

        mWaveBackgroundPaint.setColor(mWaveBackgroundColor);
        mWaveProgressPaint.setColor(mWaveProgressColor);
    }

    private int getWaveCount() {
        return mWaveform != null ? mWaveform.getWaveCount() : 0;
    }

    public void setOnSeekBarChangeListener(@Nullable OnSeekBarChangeListener l) {
        this.mListener = l;
    }

    /**
     * Sets the progress in percentage to {@code percent} value.
     * @param percent new progress value in percentage
     */
    public void setProgressInPercentage(float percent) {
        setProgressInPercentageInternal(percent, false);
    }

    private void setProgressInPercentageInternal(float percent, boolean fromUser) {
        mProgressPercentPosition = percent;
        int position = Math.round(getWaveCount() * percent) - 1;
        if (mProgressPosition != position) {
            mProgressPosition = position;
            invalidate();
        }
        if (mListener != null) {
            mListener.onProgressInPercentageChanged(this, clampPercentage(percent), fromUser);
        }
    }

    /**
     * Setups {@code waves} to display.
     * @param waves new waveform data
     */
    public void setWaveform(@Nullable int[] waves) {
        setWaveform(new WaveformImpl(waves));
    }

    /**
     * Setups {@code waves} to display and then animates it.
     * @param waves new waveform data
     * @param animate if waveform appearance needs to be animated
     */
    public void setWaveform(@Nullable int[] waves, boolean animate) {
        setWaveform(new WaveformImpl(waves), animate);
    }

    /**
     * Setups {@code waveform} to display.
     * @param waveform new waveform data
     */
    public void setWaveform(@Nullable Waveform waveform) {
        setWaveform(waveform, true);
    }

    /**
     * Setups {@code waveform} to display and then animates it.
     * @param waveform new waveform data
     * @param animate if waveform appearance needs to be animated
     */
    public void setWaveform(@Nullable Waveform waveform, boolean animate) {
        this.mWaveform = waveform;

        if (mWaveAnim != null) {
            mWaveAnim.cancel();
            mWaveAnim = null;
        }

        calculateWaveDimensions(getWaveCount());

        setProgressInPercentageInternal(0.0f, false);

        if (!animate) {
            invalidate();
        } else {
            // Animating increasing of waves from 0 to 1
            ValueAnimator newWaveAnim = ValueAnimator.ofFloat(0.0f, 1.0f);
            newWaveAnim.setDuration(mWaveAnimDur);
            newWaveAnim.setInterpolator(mWaveAnimInterpolator);
            newWaveAnim.addUpdateListener(mWaveAnimUpdateListener);
            newWaveAnim.start();

            mWaveAnim = newWaveAnim;
        }
    }

    /**
     * Sets the wave background color.
     * @param color background color
     */
    public void setWaveBackgroundColor(@ColorInt int color) {
        this.mWaveBackgroundColor = color;
        mWaveBackgroundPaint.setColor(mWaveProgressColor);
        invalidate();
    }

    /**
     * Sets the wave progress color.
     * @param color progress color
     */
    public void setWaveProgressColor(@ColorInt int color) {
        this.mWaveProgressColor = color;
        mWaveProgressPaint.setColor(mWaveProgressColor);
        invalidate();
    }

    /**
     * Sets the gap between waves in pixels.
     * NOTE: the final gap may be not the same value,
     * for example, in cases where the wave width is too small, so the wave is almost invisible.
     * @param gap the gap between waves in pixels
     */
    public void setPreferredWaveGap(int gap) {
        this.mPrefWaveGap = gap;
        calculateWaveDimensions(getWaveCount());
    }

    /**
     * Sets the wave rect corner radius in pixels.
     * @param radius the wave rect corner radius in pixels
     */
    public void setWaveCornerRadius(int radius) {
        this.mWaveCornerRadius = radius;
        invalidate();
    }

    /**
     * Calculates wave dimensions in the context of the current measured width and height and {@code waveCount} value.
     * This includes Wave width, Wave gap and Wave corner radius.
     * @param waveCount wave count for which to calculate dimensions
     */
    private void calculateWaveDimensions(int waveCount) {
        if (waveCount <= 0) {
            mWaveWidth = 0f;
            return;
        }

        final int contentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        if (contentWidth <= 0) {
            // it's probably not measured yet
            mWaveWidth = 0f;
            mWaveGap = 0f;
        }

        float waveGapCandidate = mPrefWaveGap;
        float totalGap;
        if (waveGapCandidate > 0)
            totalGap = waveGapCandidate * (waveCount - 1);
        else totalGap = 0;

        float remainingSpace = Math.abs(contentWidth - totalGap);
        float waveWidthCandidate = remainingSpace / waveCount;

        // Calculating wave width first
        if (mWaveMaxWidth > 0 && waveWidthCandidate > mWaveMaxWidth) {
            waveWidthCandidate = mWaveMaxWidth;
        }
        if (waveWidthCandidate <= 0f) {
            waveWidthCandidate = 1f;
        }

        // Calculating wave gaps then
        waveGapCandidate = (contentWidth - (waveWidthCandidate * waveCount)) / (waveCount + 1);
        if (waveGapCandidate < 0) waveGapCandidate = 0;

        mWaveWidth = waveWidthCandidate;
        mWaveGap = waveGapCandidate;

        switch (mWaveCornerType) {
            case NONE:
                mWaveCornerRadius = 0f;
                break;
            case AUTO:
                mWaveCornerRadius = mWaveWidth / 2;
                break;
            case EXACTLY:
                // the same value
                break;
        }

        invalidate();
    }

    /**
     * Returns the progress position in percent.
     * @return the progress position in percent
     */
    public float getProgressPercent() {
        return clampPercentage(mProgressPercentPosition);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) dpToPx(getContext(), 300f);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return (int) dpToPx(getContext(), 72f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWaveHeightFactor = 1f;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mWaveAnim != null) {
            mWaveAnim.cancel();
            mWaveAnim = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            calculateWaveDimensions(getWaveCount());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();

        if (!mIsTracking) {
            // Checking if the event occurs over the content area.
            // It is allowed to start tracking only if the initial touch is occurred in the content area.
            if (x < getPaddingLeft() || x > getMeasuredWidth() - getPaddingRight()
                    || y < getPaddingTop() || y > getMeasuredHeight() - getPaddingBottom()) {
                if (DEBUG) Log.d(LOG_TAG, "Motion event is not in the content area");
                return super.onTouchEvent(event);
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            mIsTracking = false;

            if (mListener != null) {
                mListener.onStopTrackingTouch(this);
            }

            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(false);
            }

            if (DEBUG) Log.d(LOG_TAG, "Stopped tracking");

            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsTracking = true;

            if (mListener != null) {
                mListener.onStartTrackingTouch(this);
            }

            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }

            if (DEBUG) Log.d(LOG_TAG, "Started tracking");
        }

        // finding the touched wave position
        final float contentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        final float dstToLeftBorder = x - getPaddingLeft() - mWaveGap;
        float percent = dstToLeftBorder / contentWidth;
        setProgressInPercentageInternal(percent, true);

        if (DEBUG) Log.d(LOG_TAG, "Tracked to percent: " + percent);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Waveform waveform = mWaveform;
        if (waveform == null || waveform.getWaveCount() <= 0) {
            return;
        }
        final int maxWave = waveform.getMaxWave();

        final int contentHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        final int leftPadding = getPaddingLeft();
        final float waveCenterY = getPaddingTop() + (float) contentHeight / 2;
        final float maxWaveHeight = contentHeight * mWaveHeightFactor;
        final float waveHalfOfWidth = mWaveWidth / 2;

        for (int i = 0; i < waveform.getWaveCount(); i++) {
            int wave = waveform.getWaveAt(i);
            float waveHeight = maxWaveHeight * ((float) wave / maxWave);
            float waveHalfOfHeight = waveHeight / 2;
            float waveCenterX = i * (mWaveWidth + mWaveGap) + mWaveGap + waveHalfOfWidth + leftPadding;

            final Paint paint;
            if (i <= mProgressPosition) {
                paint = mWaveProgressPaint;
            } else {
                paint = mWaveBackgroundPaint;
            }
            canvas.drawRoundRect(
                    waveCenterX - waveHalfOfWidth,
                    waveCenterY - waveHalfOfHeight,
                    waveCenterX + waveHalfOfWidth,
                    waveCenterY + waveHalfOfHeight,
                    mWaveCornerRadius,
                    mWaveCornerRadius,
                    paint);
        }
    }

    public interface Waveform {
        int getWaveCount();

        int getWaveAt(int index);

        int getMaxWave();
    }

    private static class WaveformImpl implements Waveform {

        final int waveCount;
        final int[] waves;
        final int maxWave;

        WaveformImpl(int[] waves) {
            this.waves = waves;
            this.waveCount = waves != null ? waves.length : 0;

            if (waves != null && waves.length > 0) {
                int maxWave = waves[0];
                for (int w : waves) {
                    if (w > maxWave) maxWave = w;
                }
                this.maxWave = maxWave;
            } else {
                this.maxWave = 0;
            }
        }

        @Override
        public int getWaveCount() {
            return waveCount;
        }

        @Override
        public int getWaveAt(int index) {
            return waves[index];
        }

        @Override
        public int getMaxWave() {
            return maxWave;
        }
    }

    /**
     * Actually, the same listener as {@link android.widget.SeekBar.OnSeekBarChangeListener}.
     */
    public interface OnSeekBarChangeListener {
        void onProgressInPercentageChanged(WaveformSeekBar seekBar, float percent, boolean fromUser);

        void onStartTrackingTouch(WaveformSeekBar seekBar);

        void onStopTrackingTouch(WaveformSeekBar seekBar);
    }
}
