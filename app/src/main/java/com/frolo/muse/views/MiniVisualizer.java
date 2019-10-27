package com.frolo.muse.views;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;

public class MiniVisualizer extends View {
    private Paint paint = new Paint();

    // without padding
    private float contentWidth;
    // without padding
    private float contentHeight;

    private int color;

    // transient data (calculated while measuring)
    private float barWidth; // in px
    private float spacing; // between bars
    private Animator animator;

    // bar heights
    private float bar1 = 0;
    private float bar2 = 0;
    private float bar3 = 0;
    private float bar4 = 0;

    private boolean animating = false;

    public MiniVisualizer(Context context) {
        this(context, null);
    }

    public MiniVisualizer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MiniVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MiniVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        color = Color.parseColor("#ff0066");
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas canvas) {
        // drawing bar 1
        canvas.drawRect(getPaddingLeft(),
                getPaddingTop() + (contentHeight - bar1),
                getPaddingLeft() + barWidth,
                contentHeight + getPaddingBottom(),
                paint);

        // drawing bar 2
        canvas.drawRect(getPaddingLeft() + barWidth + spacing,
                getPaddingTop() + (contentHeight - bar2),
                getPaddingLeft() + 2 * barWidth + spacing,
                contentHeight + getPaddingBottom(),
                paint);

        // drawing bar 3
        canvas.drawRect(getPaddingLeft() + 2 * barWidth + 2 * spacing,
                getPaddingTop() + (contentHeight - bar3),
                getPaddingLeft() + 3 * barWidth + 2 * spacing,
                contentHeight + getPaddingBottom(),
                paint);

        // drawing bar 4
        canvas.drawRect(getPaddingLeft() + 3 * barWidth + 3 * spacing,
                getPaddingTop() + (contentHeight - bar4),
                getPaddingLeft() + 4 * barWidth + 3 * spacing,
                contentHeight + getPaddingBottom(),
                paint);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final Context context = getContext();

        // desired
        int desiredWidth = (int) convertDpToPixel(35);
        int desiredHeight = (int) convertDpToPixel(70);

        // allowed
        final int measuredWidth = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);

        // saving measured sizes
        setMeasuredDimension(measuredWidth, measuredHeight);

        contentWidth = measuredWidth - getPaddingLeft() - getPaddingRight();
        contentHeight = measuredHeight - getPaddingTop() - getPaddingBottom();
        spacing = contentWidth / 10;
        barWidth = (contentWidth - 3 * spacing) / 4;

        // init positions
        bar1 = (float) (contentHeight / 3.7);
        bar2 = (float) (contentHeight / 1.5);
        bar3 = (float) (contentHeight / 5.1);
        bar4 = (float) (contentHeight / 4.1);
    }

    private void createAnimator() {
        // set to animate all the bars together
        AnimatorSet as = new AnimatorSet();

        // anim for bar1
        Animator animBar1 = createAnimator(1, 300, 0.11f, 0.15f, 0.23f, 0.31f, 0.37f, 0.41f);

        // anim for bar2
        Animator animBar2 = createAnimator(2, 715, 0.31f, 0.33f, 0.37f, 0.47f, 0.59f, 0.75f, 0.91f);

        // anim for bar3
        Animator animBar3 = createAnimator(3, 550, 0.15f, 0.21f, 0.29f, 0.41f, 0.47f, 0.51f);

        // anim for bar4
        Animator animBar4 = createAnimator(4, 425, 0.15f, 0.25f, 0.35f, 0.41f, 0.47f, 0.53f, 0.57f, 0.59f);

        as.playTogether(animBar1, animBar2, animBar3, animBar4);
        animator = as;
    }

    // bar is number of a bar (1, 2, 3 or 4)
    private Animator createAnimator(final int bar, long duration, float... values) {
        ValueAnimator animBar = ValueAnimator.ofFloat(values);
        animBar.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int h = (int) (contentHeight * (float) animation.getAnimatedValue());
                switch (bar) {
                    case 1: bar1 = h; break;
                    case 2: bar2 = h; break;
                    case 3: bar3 = h; break;
                    case 4: bar4 = h; break;
                    default: throw new IllegalArgumentException("Wrong bar: " + bar);
                }
                invalidate();
            }
        });
        animBar.setRepeatMode(ValueAnimator.REVERSE);
        animBar.setRepeatCount(ValueAnimator.INFINITE);
        animBar.setDuration(duration);
        return animBar;
    }

    private void startAnimation() {
        if (animator == null) {
            createAnimator();
        }
        animator.start();
    }

    private void pauseAnimation() {
        if (animator != null) animator.pause();
    }

    private void stopAnimation() {
        if (animator != null) animator.end();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    public void setAnimating(boolean animating) {
        if (this.animating == animating)
            return;
        this.animating = animating;
        if (animating) {
            startAnimation();
        } else {
            pauseAnimation();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (animating) {
            startAnimation();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
        animator = null;
    }

    private float convertDpToPixel(float dp){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
