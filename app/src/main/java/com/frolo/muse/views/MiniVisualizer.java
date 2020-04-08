package com.frolo.muse.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;

import com.frolo.muse.R;


public class MiniVisualizer extends View {

    // Suggested min size
    private final int mSuggestedMinWidth;
    private final int mSuggestedMinHeight;

    // Drawing tools
    private final Paint mPaint = new Paint();

    private int mColor;

    // Transient data (calculated while measuring)
    private float mBarWidth; // in px
    private float mBarSpacing; // gap width between bars

    // Bar heights
    private float mBarHeight1 = 0;
    private float mBarHeight2 = 0;
    private float mBarHeight3 = 0;
    private float mBarHeight4 = 0;

    // Animation
    private Animator mCurrAnim;
    private boolean mAnimate = false;

    private static float dp2px(@NonNull Context context, float dp){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public MiniVisualizer(Context context) {
        this(context, null);
    }

    public MiniVisualizer(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.miniVisualizerStyle);
    }

    public MiniVisualizer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mSuggestedMinWidth = (int) dp2px(context, 24f);
        mSuggestedMinHeight = (int) dp2px(context, 16f);

        final TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.MiniVisualizer, defStyleAttr, R.style.Base_AppTheme_MiniVisualizer);
        mColor = a.getColor(R.styleable.MiniVisualizer_barColor, Color.parseColor("#ff0066"));
        a.recycle();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mColor);
    }

    private int calcContentHeight() {
        if (ViewCompat.isLaidOut(this)) {
            return getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        }

        return 0;
    }

    /**
     * Creates an animation for the bar at index <code>barIndex</code>.
     * @param barIndex at which to animate the bar
     * @param duration duration of the anim
     * @param values to animate between
     * @return new animation for the bar
     */
    private Animator createAnimator(final int barIndex, final long duration, final float... values) {
        final ValueAnimator anim = ValueAnimator.ofFloat(values);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int h = (int) (calcContentHeight() * (float) animation.getAnimatedValue());
                switch (barIndex) {
                    case 1: mBarHeight1 = h; break;
                    case 2: mBarHeight2 = h; break;
                    case 3: mBarHeight3 = h; break;
                    case 4: mBarHeight4 = h; break;
                }
                invalidate();
            }
        });
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setDuration(duration);
        return anim;
    }

    @Override
    public void onDraw(Canvas canvas) {
        final int contentHeight = calcContentHeight();

        // drawing bar number 1
        canvas.drawRect(getPaddingLeft(),
                getPaddingTop() + (contentHeight - mBarHeight1),
                getPaddingLeft() + mBarWidth,
                contentHeight + getPaddingBottom(),
                mPaint);

        // drawing bar number2
        canvas.drawRect(getPaddingLeft() + mBarWidth + mBarSpacing,
                getPaddingTop() + (contentHeight - mBarHeight2),
                getPaddingLeft() + 2 * mBarWidth + mBarSpacing,
                contentHeight + getPaddingBottom(),
                mPaint);

        // drawing bar number 3
        canvas.drawRect(getPaddingLeft() + 2 * mBarWidth + 2 * mBarSpacing,
                getPaddingTop() + (contentHeight - mBarHeight3),
                getPaddingLeft() + 3 * mBarWidth + 2 * mBarSpacing,
                contentHeight + getPaddingBottom(),
                mPaint);

        // drawing bar number 4
        canvas.drawRect(getPaddingLeft() + 3 * mBarWidth + 3 * mBarSpacing,
                getPaddingTop() + (contentHeight - mBarHeight4),
                getPaddingLeft() + 4 * mBarWidth + 3 * mBarSpacing,
                contentHeight + getPaddingBottom(),
                mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        final int contentWidth = w - getPaddingLeft() - getPaddingRight();
        final int contentHeight = h - getPaddingTop() - getPaddingBottom();

        mBarSpacing = contentWidth / 10f;
        mBarWidth = (contentWidth - 3 * mBarSpacing) / 4;

        mBarHeight1 = (float) (contentHeight / 3.7);
        mBarHeight2 = (float) (contentHeight / 1.5);
        mBarHeight3 = (float) (contentHeight / 5.1);
        mBarHeight4 = (float) (contentHeight / 4.1);

        if (mCurrAnim != null) {
            // This animation is not valid anymore since the size of the view has been changed
            mCurrAnim.cancel();
        }

        setAnimateInternal(mAnimate);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return mSuggestedMinWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return mSuggestedMinHeight;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        this.mColor = color;
        invalidate();
    }

    public void setAnimate(boolean animate) {
        if (mAnimate == animate) {
            return;
        }

        mAnimate = animate;

        setAnimateInternal(animate);
    }

    private void setAnimateInternal(boolean animate) {
        if (!animate) {
            if (mCurrAnim != null) {
                mCurrAnim.cancel();
                mCurrAnim = null;
            }
        } else {
            if (mCurrAnim != null && mCurrAnim.isRunning()) {
                // It is animating already
                return;
            }

            if (mCurrAnim != null) {
                mCurrAnim.cancel();
            }

            // Creating a set of animator to animate all the bars together
            AnimatorSet set = new AnimatorSet();

            // Anim for bar number 1
            Animator animBar1 = createAnimator(1, 300, 0.11f, 0.15f, 0.23f, 0.31f, 0.37f, 0.41f);

            // Anim for bar number 2
            Animator animBar2 = createAnimator(2, 715, 0.31f, 0.33f, 0.37f, 0.47f, 0.59f, 0.75f, 0.91f);

            // Anim for bar number 3
            Animator animBar3 = createAnimator(3, 550, 0.15f, 0.21f, 0.29f, 0.41f, 0.47f, 0.51f);

            // Anim for bar number 4
            Animator animBar4 = createAnimator(4, 425, 0.15f, 0.25f, 0.35f, 0.41f, 0.47f, 0.53f, 0.57f, 0.59f);

            set.playTogether(animBar1, animBar2, animBar3, animBar4);

            set.start();

            mCurrAnim = set;
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);

        if (state instanceof SavedState) {
            SavedState savedState = (SavedState) state;
            setAnimate(savedState.animate);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);

        savedState.animate = mAnimate;

        return savedState;
    }

    private static class SavedState extends BaseSavedState {

        boolean animate;

        public SavedState(Parcel source) {
            super(source);
            animate = source.readInt() == 1;
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
            out.writeInt(animate ? 1 : 0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setAnimateInternal(mAnimate);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mCurrAnim != null) {
            mCurrAnim.cancel();
            mCurrAnim = null;
        }
    }

}
