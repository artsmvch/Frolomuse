package com.frolo.visualizerview;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;


public final class LineSpectrumRenderer extends TraceRenderer implements VisualizerView.Renderer {
    public LineSpectrumRenderer(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void renderTrace(@NonNull Canvas canvas, byte[] data, int traceIndex, @NonNull RenderParams params) {
        float barWidth = ((float) canvas.getWidth()) / params.count;
        float div = ((float) data.length) / params.count;
        params.paint.setStrokeWidth(barWidth - params.gap);

        for (int i = 0; i < params.count; i++) {
            int index = (int) Math.ceil(i * div);
            int halfBarHeight = (128 - Math.abs(data[index])) * (canvas.getHeight() / 2) / 128;
            int top = canvas.getHeight() / 2 + halfBarHeight;
            int bottom = canvas.getHeight() / 2 - halfBarHeight;
            float barX = (i * barWidth) + (barWidth / 2);
            canvas.drawLine(barX, bottom, barX, canvas.getHeight() / 2f, params.paint);
            canvas.drawLine(barX, top, barX, canvas.getHeight() / 2f, params.paint);
        }
    }
}
