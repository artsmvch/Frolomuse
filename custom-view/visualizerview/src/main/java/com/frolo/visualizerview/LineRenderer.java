package com.frolo.visualizerview;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

import com.frolo.ui.Screen;


public final class LineRenderer extends TraceRenderer implements VisualizerView.Renderer {
    private float[] points;
    @Px
    private final int lineWidth;

    public LineRenderer(@NonNull Context context) {
        super(context);
        lineWidth = Screen.dp(context, 4);
    }

    @Override
    protected void renderTrace(@NonNull Canvas canvas, byte[] data, int traceIndex, @NonNull RenderParams params) {
        params.paint.setStrokeWidth(lineWidth);
        if (points == null || points.length < data.length * 4) {
            points = new float[data.length * 4];
        }
        boolean shouldDraw = false;
        for (int i = 0; i < data.length - 1; i++) {
            if (data[i] > -128) {
                shouldDraw = true;
            }
            points[i * 4] = canvas.getWidth() * i / (data.length - 1f);
            points[i * 4 + 1] = canvas.getHeight() / 2f
                    + ((byte) (data[i] + 128)) * (canvas.getHeight() / 2f) / 128;
            points[i * 4 + 2] = canvas.getWidth() * (i + 1f) / (data.length - 1);
            points[i * 4 + 3] = canvas.getHeight() / 2f
                    + ((byte) (data[i + 1] + 128)) * (canvas.getHeight() / 2f)
                    / 128;
        }
        if (shouldDraw) {
            canvas.drawLines(points, params.paint);
        }
    }
}
