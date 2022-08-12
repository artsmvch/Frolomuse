package com.frolo.visualizerview;

import android.graphics.Canvas;
import android.graphics.Paint;

public class SpectrumRenderer extends TraceRenderer implements VisualizerView.Renderer {
    @Override
    protected void render(Canvas canvas, byte[] data, int spectrum, float density, int gap, Paint paint) {
        float barWidth = canvas.getWidth() / density;
        float div = data.length / density;
        paint.setStrokeWidth(barWidth - gap);

        for (int i = 0; i < density; i++) {
            int bytePosition = (int) Math.ceil(i * div);
            int top = canvas.getHeight() +
                    ((byte) (Math.abs(data[bytePosition]) + 128)) * canvas.getHeight() / 128;
            float barX = (i * barWidth) + (barWidth / 2);
            paint.setAlpha(255 / (spectrum + 1));
            canvas.drawLine(barX, canvas.getHeight(), barX, top, paint);
        }
    }
}
