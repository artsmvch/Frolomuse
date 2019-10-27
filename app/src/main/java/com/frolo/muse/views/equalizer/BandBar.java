package com.frolo.muse.views.equalizer;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBarWrapper;

public class BandBar extends VerticalSeekBarWrapper implements SeekBar.OnSeekBarChangeListener {
    private VerticalSeekBar seekBar;
    private OnBandLevelChangeListener listener;
    private final ValueAnimator animator = new ValueAnimator();
    private final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            int animProgress = (Integer) animation.getAnimatedValue();
            seekBar.setProgress(animProgress);
        }
    };
    private final ValueAnimator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override public void onAnimationStart(Animator animation) {
            isAnimating = true;
        }
        @Override public void onAnimationEnd(Animator animation) {
            isAnimating = false;
        }
        @Override public void onAnimationCancel(Animator animation) {
            isAnimating = false;
        }
        @Override public void onAnimationRepeat(Animator animation) { }
    };
    private boolean isAnimating = false;

    private int min = 0;
    private int max = 0;

    public BandBar(Context context) {
        this(context, null);
    }

    public BandBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BandBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        seekBar = new VerticalSeekBar(getContext());
        seekBar.setRotationAngle(VerticalSeekBar.ROTATION_ANGLE_CW_270);
        seekBar.setOnSeekBarChangeListener(this);
        this.addView(seekBar);

        animator.setDuration(170); // optimal period for this
        animator.addUpdateListener(updateListener);
        animator.addListener(animatorListener);
    }

    private int calculateHeight() {
        return max - min;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        if (min < 0) {
            min = 0;
        }

        this.min = min;
        int height = calculateHeight();
        seekBar.setMax(height);
        int progress = getProgress();
        if (progress < min) {
            progress = min;
            seekBar.setProgress(progress);
        }
    }

    public int getMax() {
        return seekBar.getMax();
    }

    public void setMax(int max) {
        this.max = max;
        int height = calculateHeight();
        this.seekBar.setMax(height);
        int progress = getProgress();
        if (progress > max) {
            progress = max;
            seekBar.setProgress(progress);
        }
    }

    public int getProgress() {
        return seekBar.getProgress() + min;
    }

    public void setProgress(int progress, boolean animate) {
        if (animate) {
            animateProgress(progress);
        } else {
            seekBar.setProgress(progress - min);
        }
    }

    public void setOnBandLevelChangeListener(OnBandLevelChangeListener listener) {
        this.listener = listener;
    }

    public static abstract class OnBandLevelChangeListener {
        public void onBandLevelChanged(BandBar bar, int progress, boolean fromUser) { }
        public void onStartTrackingTouch(BandBar bar) { }
        public void onStopTrackingTouch(BandBar bar) { }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (listener != null && !isAnimating)
            listener.onBandLevelChanged(this, progress, fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (listener != null && !isAnimating)
            listener.onStartTrackingTouch(this);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (listener != null && !isAnimating)
            listener.onStopTrackingTouch(this);
    }

    private void animateProgress(int target) {
        if (animator.isStarted()) animator.cancel(); // cancel it haven't finished yet

        animator.setIntValues(seekBar.getProgress(), target);
        animator.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !isAnimating && super.onTouchEvent(ev);
    }
}
