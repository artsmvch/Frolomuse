package com.frolo.muse.views.progress;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.frolo.muse.R;
import com.frolo.muse.Trace;

public class FroloBar extends View {
    private static final String TAG = FroloBar.class.getSimpleName();
    private static final int DEFAULT_COUNT = 11;
    // private static final int DEFAULT_SPEED = 30; // 30
    private static final int DEFAULT_HEIGHT_WIDTH_IN_DP = 48;
    private             int defHeight; // = 92;
    private             int defWidth; // = 92;
    private             int count = DEFAULT_COUNT;
    private             int activePosition;
    private             CircleData[] circles;
    private @ColorInt   int activeColor;
    private @ColorInt   int inactiveColor;
    private             int periodDuration;
    private Paint       paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean     attached = false;
    private int         speed; // = DEFAULT_SPEED;

    public FroloBar(Context context) {
        this(context, null);
    }

    public FroloBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FroloBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FroloBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        defHeight = (int) convertDpToPixel(DEFAULT_HEIGHT_WIDTH_IN_DP, context);
        defWidth = (int) convertDpToPixel(DEFAULT_HEIGHT_WIDTH_IN_DP, context);
        activePosition = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FroloBar, defStyleAttr, defStyleRes);
        periodDuration = a.getInt(R.styleable.FroloBar_periodDuration, 450);
        activeColor = a.getColor(R.styleable.FroloBar_activeColor, Color.RED);
        inactiveColor = a.getColor(R.styleable.FroloBar_inactiveColor, Color.WHITE);
        count = a.getInt(R.styleable.FroloBar_pointCount, 11);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Trace.d(TAG, "drawing");
        for (int i = 0; i < circles.length; i++) {
            CircleData current = circles[i];
            int twinPosition = i + activePosition;
            if (twinPosition >= circles.length) twinPosition -= circles.length;
            CircleData twin = circles[twinPosition];
            paint.setColor(twin.color);
            canvas.drawCircle(current.x, current.y, twin.radius, paint);
        }
        moveClockwise();
        postInvalidateDelayed(speed);
    }

    private void moveClockwise() {
        activePosition--;
        if (activePosition < 0) activePosition = count - 1;
    }

    private void moveCounterclockwise() {
        activePosition++;
        if (activePosition >= count) activePosition = 0;
    }

    public void set(int count, int activePosition) {
        if (activePosition < 0 || activePosition >= count)
            throw new IllegalArgumentException("activePosition is out of bounds");
        if (count < 2)
            throw new IllegalArgumentException("count should be at least 2");
        this.count = count;
        this.activePosition = activePosition;
        invalidate();
    }

    public void setColors(int activeColor, int inactiveColor) {
        this.activeColor = activeColor;
        this.inactiveColor = inactiveColor;
        invalidate();
    }

    public void setActiveColor(int activeColor) {
        this.activeColor = activeColor;
        invalidate();
    }

    public void setInactiveColor(int inactiveColor) {
        this.inactiveColor = inactiveColor;
        invalidate();
    }

    public void setSpeed(int milliseconds) {
        this.speed = milliseconds;
    }

    /**
     * prepares Data for {@link FroloBar#onDraw(Canvas)}
     */
    private void resolveData(int actualWidth, int actualHeight) {
        circles = new CircleData[count];
        int height = actualHeight;
        int width = actualWidth;
        int[] interpolated = interpolate(activeColor, inactiveColor, count);
        float cx = height / 2;
        float cy = width / 2;
        float minRadius = resolveMinRadius(height, width);
        float maxRadius = resolveMaxRadius(height, width);
        float r0 = Math.min(height, width) / 2 - maxRadius;
        double angleDelta = 360 / count;
        double angle = angleDelta * activePosition;
        float radiusDelta = ((float) (maxRadius - minRadius)) /count;
        for (int i = 0; i < count; i++) {
            // x = radius *  cos(angle)
            // y = radius *  sin(angle)
            CircleData data = new CircleData();
            data.x = cx + (float) (r0 * Math.cos(toRadian(angle + angleDelta * i)));
            data.y = cy + (float) (r0 * Math.sin(toRadian(angle + angleDelta * i)));
            data.radius = minRadius + radiusDelta * i;
            data.color = interpolated[i];
            circles[i] = (data);
        }
        char divider = '\n';
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < interpolated.length; i++) {
            sb.append(i).append('=').append(interpolated[i]).append("; ");
        }

        String msg = new StringBuilder()
                .append("Data resolved: ")
                .append("height=")
                .append(height)
                .append(", width=")
                .append(width).append(divider)
                .append("cx=").append(cx).append(divider)
                .append("cy=").append(cy).append(divider)
                .append("minRad=").append(minRadius).append(divider)
                .append("maxRad=").append(maxRadius).append(divider)
                .append("r0=").append(r0).append(divider)
                .append("interpolated: ").append(sb.toString())
                .toString();
        Trace.d(TAG, msg);
    }

    private int getActualSpaceHeight() {
        int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        return height > 0 ? height : 0;
    }

    private int getActualSpaceWidth() {
        int width = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        return width > 0 ? width : 0;
    }

    private float resolveMinRadius(int actualSpaceHeight, int actualSpaceWidth) {
        float max = Math.max(actualSpaceHeight, actualSpaceWidth);
        return max > 30 ? 1 : 1;
    }

    private float resolveMaxRadius(int actualSpaceHeight, int actualSpaceWidth) {
        float max = Math.max(actualSpaceHeight, actualSpaceWidth);
        return max > 56 ? (float) (max / (14)) : 4;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Trace.d(TAG, "measuring");
        speed = periodDuration / count;
        final int measuredWidth = resolveSizeAndState(defWidth, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(defHeight, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
        resolveData(measuredWidth, measuredHeight);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        Trace.d(TAG, "attached");
        // start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        Trace.d(TAG, "detached");
        // stop();
    }

    private class CircleData {
        float       x;
        float       y;
        float       radius;
        @ColorInt   int color;
    }

    private double toRadian(double angleInDegrees) {
        return angleInDegrees * Math.PI / 180F;
    }

    protected static int blendColors(int from, int to, float ratio) {
        final float inverseRatio = 1f - ratio;

        final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

        return Color.rgb((int) r, (int) g, (int) b);
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
            int blended = blendColors(c1, c2, iFactor);
            container[i] = blended;
        }
    }

    private static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
