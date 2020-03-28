package com.frolo.muse.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;


/**
 * Helper class to easily make snapshots of UI components.
 */
public final class Snapshots {
    private Snapshots() {
    }

    /**
     * Makes a {@link Bitmap} snapshot from the given <code>view</code>.
     * NOTE: <code>view</code>> should be laid out when calling this method.
     * @param view from which to make a snapshot
     * @return a snapshot represented by {@link Bitmap}
     */
    @Nullable
    public static Bitmap make(@NonNull final View view) {
        if (!ViewCompat.isLaidOut(view)) {
            // View is not laid out
            return null;
        }

        view.setDrawingCacheEnabled(true);
        final Bitmap drawingCache = view.getDrawingCache();
        view.setDrawingCacheEnabled(false);

        if (drawingCache != null && !drawingCache.isRecycled()) {
            // OK, we can use the drawing cache
            return Bitmap.createBitmap(drawingCache);
        }

        // Drawing cache is invalid, creating a snapshot on my own
        final int width = view.getMeasuredWidth();
        final int height = view.getMeasuredHeight();
        final Bitmap bmp = Bitmap.createBitmap(width , height, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(bmp);
        view.draw(c);
        return bmp;
    }

}
