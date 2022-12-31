package com.frolo.visualizerview;

import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;


public final class SpectrumRenderer extends TraceRenderer implements VisualizerView.Renderer {
    public SpectrumRenderer(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void renderTrace(@NonNull Canvas canvas, byte[] data, int traceIndex, @NonNull RenderParams params) {
        float barWidth = ((float) canvas.getWidth()) / params.count;
        float div = ((float) data.length) / params.count;
        params.paint.setStrokeWidth(barWidth - params.gap);

        for (int i = 0; i < params.count; i++) {
            int bytePosition = (int) Math.ceil(i * div);
            int top = canvas.getHeight() +
                    ((byte) (Math.abs(data[bytePosition]) + 128)) * canvas.getHeight() / 128;
            float barX = (i * barWidth) + (barWidth / 2);
            canvas.drawLine(barX, canvas.getHeight(), barX, top, params.paint);
        }
    }
}
