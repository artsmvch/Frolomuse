package com.frolo.visualizerview;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

import com.frolo.ui.Screen;


public final class CircleRenderer extends TraceRenderer implements VisualizerView.Renderer {
    private float[] points;
    @Px
    private float radius;
    @Px
    private final int lineWidth;

    public CircleRenderer(@NonNull Context context) {
        super(context);
        radius = Screen.dp(context, 24f);
        lineWidth = Screen.dp(context, 3);
    }

    @Override
    public void measure(int width, int height) {
        radius = Math.min(width, height) / 2f;
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
            float coefficient1 = (-Math.abs(data[i]) + 128) / 128f;
            float coefficient2 = (-Math.abs(data[i + 1]) + 128) / 128f;
            float radius1 = radius * coefficient1;
            float radius2 = radius * coefficient2;
            points[i * 4] = (float) (canvas.getWidth() / 2
                    + radius1
                    * Math.cos(Math.toRadians(angle)));

            points[i * 4 + 1] = (float) (canvas.getHeight() / 2
                    + radius1
                    * Math.sin(Math.toRadians(angle)));

            points[i * 4 + 2] = (float) (canvas.getWidth() / 2
                    + radius2
                    * Math.cos(Math.toRadians(angle + 1)));

            points[i * 4 + 3] = (float) (canvas.getHeight() / 2
                    + radius2
                    * Math.sin(Math.toRadians(angle + 1)));
        }
        canvas.drawLines(points, params.paint);
    }
}
