package com.frolo.muse.views.visualizer;

import android.graphics.Canvas;
import android.graphics.Paint;


public class CircleRenderer extends TraceRenderer implements VisualizerView.Renderer {
    private float[] points;
    private float radiusMultiplier = 0.5f;
    private int lineWidth = 5;

    @Override
    protected void render(Canvas canvas, byte[] data, int spectrum, float density, int gap, Paint paint) {
        paint.setStrokeWidth(lineWidth);
        paint.setAlpha(255 / (spectrum + 1));
        if (points == null || points.length < data.length * 4) {
            points = new float[data.length * 4];
        }
        double angleCake = 360d / (data.length - 1);

        for (int i = 0; i < data.length - 1; i++) {
            double angle = angleCake * i;
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
        canvas.drawLines(points, paint);
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getRadiusMultiplier() {
        return radiusMultiplier;
    }

    public void setRadiusMultiplier(float radiusMultiplier) {
        this.radiusMultiplier = radiusMultiplier;
    }
}
