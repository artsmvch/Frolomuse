package com.frolo.visualizerview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.LinkedList;


/**
 * Base renderer to be inherited;
 * Represents several fading spectres;
 * Each spectrum should be drawn in {@link #render(Canvas, byte[], int, float, int, Paint)} method;
 */
public class TraceRenderer implements VisualizerView.Renderer {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float density = 43; // def 50 ?
    private int gap = 5;
    private int color;

    // saving last 10 spectres
    private final int maxSpectrumCount = 7;
    private final LinkedList<byte[]> spectres = new LinkedList<>();

    public TraceRenderer() {
        color = Color.parseColor("#1686B6");
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
    }

    /**
     * New data is coming, we need to setPlayingPositionAndState the queue of spectres;
     * @param data new given data
     */
    private void updateInternal(byte[] data) {
        byte[] spectrum = null;
        // checking if we the queue is full and we can reuse the last spectrum
        if (spectres.size() == maxSpectrumCount) {
            spectrum = spectres.removeLast(); // reuse the last one

            // checking if the lengths are equal
            if (spectrum.length != data.length) {
                // recreating if the length of the old spectrum doesn't equal the new one
                spectrum = new byte[data.length];
            }

            // copying src from the buffer
            System.arraycopy(data, 0, spectrum, 0, data.length);
        } else {
            spectrum = data;
        }

        spectres.addFirst(spectrum);
    }

    @Override
    public final void render(@NonNull Canvas canvas, @NonNull byte[] data) {
        // updating the queue
        updateInternal(data);

        int i = 0;
        // rendering each spectrum if the queue
        for (byte[] spectrum : spectres) {
            render(canvas, spectrum, i++, density, gap, paint);
        }
    }

    /**
     * renders just one spectrum by given params;
     * it's ALLOWED AND NECESSARY for inheritors to implement rendering of each spectrum;
     * @param canvas on which the drawing is happening
     * @param data that comes from source output
     * @param spectrum number of spectrum in the queue
     * @param density of spectres
     * @param gap between spectres
     * @param paint to use while drawing
     */
    protected void render(Canvas canvas, byte[] data, int spectrum, float density, int gap, Paint paint) {
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
        paint.setColor(color);
    }

    public void setDensity(int density) {
        assertValidDensity(density);
        this.density = density;
    }

    public void setGap(int gap) {
        assertValidGap(gap);
        this.gap = gap;
    }

    public @ColorInt int getColor() {
        return color;
    }

    public float getDensity() {
        return density;
    }

    public int getGap() {
        return gap;
    }

    private void assertValidDensity(float density) {
        if (density < 10f || density > 255f)
            throw new IllegalArgumentException("Wrong density: " + density + ". Allowed to use values [10..255]");
    }

    private void assertValidGap(int gap) {
        if (gap < 0 || gap > 50)
            throw new IllegalArgumentException("Wrong gap: " + gap + ". Allowed to use values [0..50]");
    }
}
