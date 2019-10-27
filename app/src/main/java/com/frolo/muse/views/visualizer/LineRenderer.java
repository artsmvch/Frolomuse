package com.frolo.muse.views.visualizer;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.core.graphics.ColorUtils;


public class LineRenderer extends TraceRenderer implements VisualizerView.Renderer {
    private float[] points;
    private int lineWidth = 3;

    @Override
    protected void render(Canvas canvas, byte[] data, int spectrum, float density, int gap, Paint paint) {
        paint.setStrokeWidth(lineWidth);
        if (points == null || points.length < data.length * 4) {
            points = new float[data.length * 4];
        }
        for (int i = 0; i < data.length - 1; i++) {
            points[i * 4] = canvas.getWidth() * i / (data.length - 1);
            points[i * 4 + 1] = canvas.getHeight() / 2
                    + ((byte) (data[i] + 128)) * (canvas.getHeight() / 2) / 128;
            points[i * 4 + 2] = canvas.getWidth() * (i + 1) / (data.length - 1);
            points[i * 4 + 3] = canvas.getHeight() / 2
                    + ((byte) (data[i + 1] + 128)) * (canvas.getHeight() / 2)
                    / 128;
        }
        canvas.drawLines(points, paint);
    }

    private static int[] interpolate(int c1, int c2, int length) {
        int[] array = new int[length];
        interpolate(array, c1, c2, length);
        return array;
    }

    private static void interpolate(int[] container, int c1, int c2, int length) {
        float stepFactor = ((float) 1) / (length - 1);
        for (int i = 0; i < length; i++) {
            float iFactor = stepFactor * i;
            int blended = ColorUtils.blendARGB(c1, c2, iFactor);
            container[i] = blended;
        }
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }
}
