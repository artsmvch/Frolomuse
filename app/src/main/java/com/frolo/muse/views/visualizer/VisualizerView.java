package com.frolo.muse.views.visualizer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Visualizer View draws byte data that comes from an audio input;
 * The view delegates drawing to the {@link Renderer};
 * {@link Renderer} renders waveform or fft as well;
 */
public class VisualizerView extends View {
    private static final String TAG = VisualizerView.class.getSimpleName();

    private Rect mRect = new Rect();
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Context context = getContext();
        final int defWidth = (int) convertDpToPixel(100);
        final int defHeight = (int) convertDpToPixel(100);
        final int measuredWidth = resolveSizeAndState(defWidth, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(defHeight, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);

        mRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // if there is renderer and data to be rendered
        if (renderer != null && data != null)
            renderer.render(canvas, data);
    }

    public void setData(byte[] data) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
        invalidate();
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    // Delegate to render data (waveform or fft)
    public interface Renderer {
        /**
         * Draws wave data on canvas;
         * The data is passed from the buffer so the renderer can hold reference to the data;
         * @param canvas where the drawing is happening;
         * @param data data from the audio source;
         */
        void render(@NonNull Canvas canvas, @NonNull byte[] data);
    }

    private float convertDpToPixel(float dp){
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
