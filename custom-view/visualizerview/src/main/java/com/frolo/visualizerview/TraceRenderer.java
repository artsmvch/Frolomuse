package com.frolo.visualizerview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;

import com.frolo.ui.Screen;
import com.frolo.ui.StyleUtils;

import java.util.LinkedList;


public abstract class TraceRenderer implements VisualizerView.Renderer {
    private static final int DEFAULT_SPECTRUM_COUNT = 8;

    @NonNull
    private final Context context;
    private final int spectrumCount;
    @NonNull
    private final LinkedList<byte[]> spectres = new LinkedList<>();
    @NonNull
    private final RenderParams renderParams;

    public TraceRenderer(@NonNull Context context) {
        this(context, DEFAULT_SPECTRUM_COUNT);
    }

    public TraceRenderer(@NonNull Context context, int spectrumCount) {
        this.context = context;
        this.spectrumCount = spectrumCount;
        this.renderParams = new RenderParams(context);
    }

    private void next(byte[] data) {
        if (spectrumCount == 0) {
            return;
        }
        byte[] spectrum = null;
        if (spectres.size() == spectrumCount) {
            // Re-using the last one
            spectrum = spectres.removeLast();
            if (spectrum.length != data.length) {
                spectrum = new byte[data.length];
            }
            System.arraycopy(data, 0, spectrum, 0, data.length);
        } else {
            spectrum = data;
        }
        spectres.addFirst(spectrum);
    }

    @NonNull
    protected final Context getContext() {
        return context;
    }

    @Override
    public void measure(int width, int height) {
    }

    @Override
    public final void render(@NonNull Canvas canvas, @NonNull byte[] data) {
        next(data);
        int spectrumIndex = 0;
        for (byte[] spectrum : spectres) {
            renderParams.paint.setAlpha(255 / (spectrumIndex + 1));
            renderSpectrum(canvas, spectrum, spectrumIndex++, renderParams);
        }
    }

    /**
     * Renders just one spectrum for the given params.
     * @param canvas to draw
     * @param data visualizer data
     * @param spectrumIndex index of the spectrum in the queue
     * @param params renderer params
     */
    protected void renderSpectrum(@NonNull Canvas canvas, byte[] data, int spectrumIndex, @NonNull RenderParams params) {
    }

    protected static class RenderParams {
        @ColorInt
        final int color;
        @Px
        final int gap;
        final int count;
        @NonNull
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        RenderParams(@NonNull Context context) {
            color = StyleUtils.resolveColor(context, android.R.attr.colorAccent);
            paint.setColor(color);
            paint.setStyle(Paint.Style.FILL);
            gap = Screen.dp(context, 2);
            count = 48;
        }
    }
}
