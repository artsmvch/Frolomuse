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
    private static final int DEFAULT_TRACE_COUNT = 8;

    @NonNull
    private final Context context;
    private final int traceCount;
    @NonNull
    private final LinkedList<byte[]> traces = new LinkedList<>();
    @NonNull
    private final RenderParams renderParams;

    public TraceRenderer(@NonNull Context context) {
        this(context, DEFAULT_TRACE_COUNT);
    }

    public TraceRenderer(@NonNull Context context, int traceCount) {
        this.context = context;
        this.traceCount = traceCount;
        this.renderParams = new RenderParams(context);
    }

    private void next(byte[] data) {
        if (traceCount == 0) {
            return;
        }
        byte[] trace = null;
        if (traces.size() == traceCount) {
            // Re-using the last one
            trace = traces.removeLast();
            if (trace.length != data.length) {
                trace = new byte[data.length];
            }
            System.arraycopy(data, 0, trace, 0, data.length);
        } else {
            trace = data;
        }
        traces.addFirst(trace);
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
        int traceIndex = 0;
        for (byte[] trace : traces) {
            renderParams.paint.setAlpha(255 / (traceIndex + 1));
            renderTrace(canvas, trace, traceIndex++, renderParams);
        }
    }

    /**
     * Renders just one trace for the given trace index.
     * @param canvas to draw
     * @param data visualizer data
     * @param traceIndex index of the trace
     * @param params renderer params
     */
    protected void renderTrace(@NonNull Canvas canvas, byte[] data, int traceIndex, @NonNull RenderParams params) {
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
