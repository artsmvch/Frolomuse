package com.frolo.visualizerview;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

import com.frolo.ui.Screen;


public final class CircleRenderer extends TraceRenderer implements VisualizerView.Renderer {
    private float[] points;
    private final float radiusMultiplier = 0.5f;
    @Px
    private final int lineWidth;

    public CircleRenderer(@NonNull Context context) {
        super(context);
        lineWidth = Screen.dp(context, 3);
    }

    @Override
    protected void renderTrace(@NonNull Canvas canvas, byte[] data, int traceIndex, @NonNull RenderParams params) {
        params.paint.setStrokeWidth(lineWidth);
        if (points == null || points.length < data.length * 4) {
            points = new float[data.length * 4];
        }
        double angleStep = 360d / (data.length - 1);
        for (int i = 0; i < data.length - 1; i++) {
            double angle = angleStep * i;
            points[i * 4] = (float) (canvas.getWidth() / 2
                    + Math.abs(data[i])
                    * radiusMultiplier
                    * Math.cos(Math.toRadians(angle)));
            points[i * 4 + 1] = (float) (canvas.getHeight() / 2
                    + Math.abs(data[i])
                    * radiusMultiplier
                    * Math.sin(Math.toRadians(angle)));

            points[i * 4 + 2] = (float) (canvas.getWidth() / 2
                    + Math.abs(data[i + 1])
                    * radiusMultiplier
                    * Math.cos(Math.toRadians(angle + 1)));

            points[i * 4 + 3] = (float) (canvas.getHeight() / 2
                    + Math.abs(data[i + 1])
                    * radiusMultiplier
                    * Math.sin(Math.toRadians(angle + 1)));
        }
        canvas.drawLines(points, params.paint);
    }
}
