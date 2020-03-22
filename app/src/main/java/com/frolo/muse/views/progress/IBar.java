package com.frolo.muse.views.progress;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;

import com.frolo.muse.R;

/**
 * Progress Bar designed like Ios Progress Bar;
 * Usually it has 20 bars;
 * the first 5 are active so we interpolate colors for these bars only;
 */
public class IBar extends View {
    private static final float DEFAULT_SIZE_IN_DP = 24f;

    private static final int DEFAULT_ACTIVE_COLOR =
            Color.parseColor("#BBBBBB");

    private static final int DEFAULT_INACTIVE_COLOR =
            Color.parseColor("#15F5F5F5");

    private final int count = 12;
    private @ColorInt int activeColor;
    private @ColorInt int inactiveColor;
    // rect corners
    private final long speedPeriod = 60L;
    private int rx = 15;
    private int ry = 15;

    // The bar presentation is based on the interpolated colors;
    // in particular, on its length;
    private @ColorInt int[] colors;
    private final float angle = (float) (360 / count);
    private int currPos = 0;

    // paint tools
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public IBar(Context context) {
        this(context, null);
    }

    public IBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.iBarStyle);
    }

    public IBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, R.style.Base_IBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("ResourceType")
    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final Context context = getContext();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IBar, defStyleAttr, defStyleRes);
        activeColor = a.getColor(R.styleable.IBar_activeColor, DEFAULT_ACTIVE_COLOR);
        inactiveColor = a.getColor(R.styleable.IBar_inactiveColor, DEFAULT_INACTIVE_COLOR);
        a.recycle();

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Trace.d(TAG, "drawing");

        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        //canvas.drawRect(rect, paint);
        //canvas.save();
        for (int i = 0; i < count; i++) {
            int pos = currPos - i;
            if (pos < 0) pos += count;
            paint.setColor(colors[pos]);
            //Trace.d(TAG, "angle=" + currAngle);
            canvas.rotate(angle, cx, cy);
            canvas.drawRoundRect(rect, rx, ry, paint);
        }
        //canvas.restore();
        moveCounterClockwise();

        postInvalidateDelayed(speedPeriod);
    }

    private void moveClockwise() {
        currPos--;
        if (currPos <= 0) currPos = count - 1;
    }

    private void moveCounterClockwise() {
        currPos++;
        if (currPos >= count) currPos = 0;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int defSize = (int) convertDpToPixel(DEFAULT_SIZE_IN_DP, getContext());
        final int measuredWidth = resolveSizeAndState(defSize, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(defSize, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resolveSizes(w, h);
    }

    @SuppressLint("DefaultLocale")
    private void resolveSizes(int measuredWidth, int measuredHeight) {
        float contentWidth = measuredWidth - getPaddingLeft() - getPaddingRight();
        float contentHeight = measuredHeight - getPaddingBottom() - getPaddingTop();
        float halfOfContentWidth = contentWidth / 2;
        float halfOfContentHeight = contentHeight / 2;
        float r = Math.min(halfOfContentWidth, halfOfContentHeight);
        float cx = getPaddingLeft() + halfOfContentWidth;
        float cy = getPaddingTop() + halfOfContentHeight;
        // Bars have margin a from the center
        float marginR = (int) (r * 0.45f);
        float barWidthHalf = (int) (r / (12f));

        rect.set(cx + marginR, cy - barWidthHalf, cx + r, cy + barWidthHalf);

        // preparing corners
        rx = (int) (r / 10);
        ry = (int) (r / 10);

        // preparing colors
        int activesCount = count / 1;
        final int[] actives = new int[activesCount];
        interpolate(actives, activeColor, inactiveColor, activesCount);
        colors = new int[count];
        System.arraycopy(actives, 0, colors, 0, activesCount);
        for (int i = activesCount; i < count; i++) {
            colors[i] = inactiveColor;
        }
    }

    private static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    protected static int blendColors(int from, int to, float ratio) {
        return ColorUtils.blendARGB(from, to, ratio);
    }

    protected static int[] interpolate(int c1, int c2, int length) {
        int[] array = new int[length];
        interpolate(array, c1, c2, length);
        return array;
    }

    protected static void interpolate(int[] container, int c1, int c2, int length) {
        float stepFactor = ((float) 1) / (length - 1);
        for (int i = 0; i < length; i++) {
            float iFactor = stepFactor * i;
            int blended = blendColors(c1, c2, (float) Math.sqrt(iFactor));
            container[i] = blended;
        }
    }
}
