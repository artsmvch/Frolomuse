package com.frolo.muse.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frolo.muse.util.BitmapUtil;
import com.frolo.muse.util.CenterCropBitmapDrawable;


/**
 * Helper class to easily make snapshots of UI components.
 */
public final class Snapshots {
    private Snapshots() {
    }

    public interface BitmapAsyncListener {
        void onGenerated(@Nullable Bitmap bitmap);
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

    /**
     * Asynchronously makes a {@link Bitmap} snapshot from the given <code>view</code>.
     * The result is passed through the <code>listener</code>.
     *
     * If <code>background</code> drawable is not null
     * then it will be used as the background for the snapshot.
     * Otherwise, <code>backgroundColor</code> will be used as the background for the snapshot.
     *
     * NOTE: <code>view</code>> should be laid out when calling this method.
     * @param view from which to make a snapshot
     * @param background background for the snapshot, may be null
     * @param backgroundColor background color for the snapshot, used if <code>background</code> is null
     * @param listener listener to know when the snapshot is ready
     * @return a snapshot represented by {@link Bitmap}
     */
    @NonNull
    public static AsyncTask<?, ?, Bitmap> makeAsync(
        @NonNull    final View view,
        @Nullable   final Drawable background,
        @ColorInt   final int backgroundColor,
        @NonNull    final BitmapAsyncListener listener
    ) {
        final Resources resources = view.getResources();

        final int width = view.getMeasuredWidth();
        final int height = view.getMeasuredHeight();

        return new AsyncTask<Object, Object, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Object... objects) {

                final Bitmap result = Bitmap.createBitmap(width , height, Bitmap.Config.ARGB_8888);
                final Canvas resultCanvas = new Canvas(result);

                if (background != null) {
                    try {
                        // If the intrinsic width and height are positive, then we cat try to create a center cropped bitmap from it
                        if (background.getIntrinsicWidth() > 0 && background.getIntrinsicHeight() > 0) {
                            final Bitmap backgroundBitmap = BitmapUtil.getBitmap(background);
                            final Drawable centerCropBackground =
                                    new CenterCropBitmapDrawable(resources, backgroundBitmap, width, height);
                            centerCropBackground.draw(resultCanvas);
                            // TODO: check
                        } else {
                            // If it's a ColorDrawable then we can simply draw its color on the canvas
                            if (background instanceof ColorDrawable) {
                                resultCanvas.drawColor(((ColorDrawable) background).getColor());
                            } else {
                                background.draw(resultCanvas);
                            }
                        }
                    } catch (Throwable ignored) {
                        // It failed, drawing the background color
                        resultCanvas.drawColor(backgroundColor);
                    }
                } else {
                    resultCanvas.drawColor(backgroundColor);
                }

                return result;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    final Canvas canvas = new Canvas(bitmap);
                    view.draw(canvas);
                }

                listener.onGenerated(bitmap);
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
