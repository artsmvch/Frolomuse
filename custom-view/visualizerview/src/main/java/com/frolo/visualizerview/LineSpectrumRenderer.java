package com.frolo.visualizerview;

import android.graphics.Canvas;
import android.graphics.Paint;


public class LineSpectrumRenderer extends TraceRenderer implements VisualizerView.Renderer {
    @Override
    protected void render(Canvas canvas, byte[] data, int spectrum, float density, int gap, Paint paint) {
        paint.setAlpha(255 / (spectrum + 1));
        float barWidth = canvas.getWidth() / density;
        float div = data.length / density;
        canvas.drawLine(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2, paint);
        paint.setStrokeWidth(barWidth - gap);

        for (int i = 0; i < density; i++) {
            int bytePosition = (int) Math.ceil(i * div);
            int top = canvas.getHeight() / 2
                    + (128 - Math.abs(data[bytePosition]))
                    * (canvas.getHeight() / 2) / 128;

            int bottom = canvas.getHeight() / 2
                    - (128 - Math.abs(data[bytePosition]))
                    * (canvas.getHeight() / 2) / 128;

            float barX = (i * barWidth) + (barWidth / 2);
            canvas.drawLine(barX, bottom, barX, canvas.getHeight() / 2, paint);
            canvas.drawLine(barX, top, barX, canvas.getHeight() / 2, paint);
        }
    }
}
