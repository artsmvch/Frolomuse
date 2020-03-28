package com.frolo.muse.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * Helper class to easily make snapshots of UI components.
 */
public final class Snapshots {
    private Snapshots() {
    }

    /**
     * Makes a {@link Bitmap} snapshot from the given <code>view</code>.
     * NOTE: <code>view</code>> should be laid out when calling this method.
     * NOTE: the background of the result snapshot will be transparent,
     * so if the view has no background, then only its content will be visible.
     * @param view from which to make a snapshot
     * @return a snapshot represented by {@link Bitmap}
     */
    @Nullable
    public static Bitmap make(@NonNull final View view) {
        return make(view, Color.TRANSPARENT);
    }

    /**
     * Makes a {@link Bitmap} snapshot from the given <code>view</code>.
     * The background of the result snapshot is colored in <code>backgroundColor</code>.
     * NOTE: <code>view</code>> should be laid out when calling this method.
     * @param view from which to make a snapshot
     * @param backgroundColor in which snapshot's background will be colored
     * @return a snapshot represented by {@link Bitmap}
     */
    @Nullable
    public static Bitmap make(@NonNull final View view, @ColorInt int backgroundColor) {

        // First checking the drawing cache
        view.setDrawingCacheEnabled(true);
        final Bitmap drawingCache = view.getDrawingCache();
        view.setDrawingCacheEnabled(false);

        if (drawingCache != null && !drawingCache.isRecycled()) {
            // OK, we can use the drawing cache
            return Bitmap.createBitmap(drawingCache);
        }

        // The drawing cache is invalid, creating a snapshot on my own
        final int width = view.getMeasuredWidth();
        final int height = view.getMeasuredHeight();
        final Bitmap bmp = Bitmap.createBitmap(width , height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        canvas.drawColor(backgroundColor);
        view.draw(canvas);
        return bmp;
    }

}
