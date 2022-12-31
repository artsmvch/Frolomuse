package com.frolo.visualizerview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * VisualizerView draws sound wave data represented by an array of bytes.
 * Usually works in conjunction with {@link android.media.audiofx.Visualizer}.
 * The rendering method is determined by {@link Renderer}.
 */
public class VisualizerView extends View {
    private static final String TAG = VisualizerView.class.getSimpleName();

    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 120;

    private Renderer renderer;
    private byte[] data;

    public VisualizerView(Context context) {
        this(context, null);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init(attrs);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private float convertDpToPixel(float dp){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int defaultWidth = (int) convertDpToPixel(DEFAULT_WIDTH);
        final int defaultHeight = (int) convertDpToPixel(DEFAULT_HEIGHT);
        final int measuredWidth = resolveSizeAndState(defaultWidth, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(defaultHeight, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        super.layout(l, t, r, b);
        if (renderer != null) {
            renderer.measure(r - l, b - t);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (renderer != null && data != null) {
            renderer.render(canvas, data);
        }
    }

    public void setData(byte[] data) {
        if (data != null) {
            this.data = new byte[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);
        } else {
            this.data = null;
        }
        invalidate();
    }

    @Nullable
    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(@Nullable Renderer renderer) {
        this.renderer = renderer;
        requestLayout();
        invalidate();
    }

    public interface Renderer {
        void measure(int width, int height);
        void render(@NonNull Canvas canvas, @NonNull byte[] data);
    }
}
