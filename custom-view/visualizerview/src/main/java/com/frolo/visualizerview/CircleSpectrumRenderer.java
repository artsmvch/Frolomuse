package com.frolo.visualizerview;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Px;

import com.frolo.ui.Screen;


public final class CircleSpectrumRenderer extends TraceRenderer implements VisualizerView.Renderer {
    private float[] points;
    @Px
    private final int lineWidth;
    @Px
    private float radius;
    @Px
    private float innerRadius;

    public CircleSpectrumRenderer(@NonNull Context context) {
        super(context);
        lineWidth = Screen.dp(context, 3);
        radius = Screen.dp(context, 24);
        innerRadius = radius / 3f;
    }

    @Override
    public void measure(int width, int height) {
        radius = Math.min(width, height) / 2f;
        innerRadius = radius / 3f;
    }

    @Override
    protected void renderTrace(@NonNull Canvas canvas, byte[] data, int traceIndex, @NonNull RenderParams params) {
        params.paint.setStrokeWidth(lineWidth);
        if (points == null || points.length < data.length * 4) {
            points = new float[data.length * 4];
        }

        double angleCake = 360d / (data.length - 1);

        for (int i = 0; i < data.length - 1; i++) {
            double angle = angleCake * i;
            float coefficient = (-Math.abs(data[i]) + 128) / 128f;
            float delta = (radius - innerRadius) * coefficient;

            points[i * 4] = (float) (canvas.getWidth() / 2
                    + innerRadius
                    * Math.cos(Math.toRadians(angle)));

            points[i * 4 + 1] = (float) (canvas.getHeight() / 2
                    + innerRadius
                    * Math.sin(Math.toRadians(angle)));

            points[i * 4 + 2] = (float) (canvas.getWidth() / 2
                    + (innerRadius + delta)
                    * Math.cos(Math.toRadians(angle)));

            points[i * 4 + 3] = (float) (canvas.getHeight() / 2
                    + (innerRadius + delta)
                    * Math.sin(Math.toRadians(angle)));
        }
        canvas.drawLines(points, params.paint);
    }
}
