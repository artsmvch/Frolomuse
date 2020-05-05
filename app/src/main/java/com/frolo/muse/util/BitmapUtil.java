package com.frolo.muse.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.frolo.muse.ThreadStrictMode;


public final class BitmapUtil {
    private BitmapUtil() {
    }

    @WorkerThread
    public static Bitmap getBitmap(Drawable drawable, int overrideWidth, int overrideHeight) {
        ThreadStrictMode.assertBackground();

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.getWidth() != overrideWidth && bitmap.getHeight() != overrideHeight) {
                return Bitmap.createScaledBitmap(bitmap, overrideWidth, overrideHeight, true);
            } else {
                return bitmap;
            }
        }

        final Rect oldBounds = new Rect(drawable.getBounds());

        //final Bitmap bitmap = Bitmap.createBitmap(overrideWidth, overrideHeight, Bitmap.Config.ARGB_8888);
        final Bitmap bitmap = createTransparentBitmap(overrideWidth, overrideHeight, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, overrideWidth, overrideHeight);
        drawable.draw(new Canvas(bitmap));

        drawable.setBounds(oldBounds.left, oldBounds.top, oldBounds.right, oldBounds.bottom);
        return bitmap;
    }

    @WorkerThread
    public static Bitmap getBitmap(Drawable drawable) {
        ThreadStrictMode.assertBackground();

        final int width = drawable.getIntrinsicWidth();
        final int height = drawable.getIntrinsicHeight();
        return getBitmap(drawable, width, height);
    }

    public static Bitmap createTransparentBitmap(int width, int height, Bitmap.Config config) {
        Bitmap newBitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        return newBitmap;
    }

    @WorkerThread
    public static Bitmap createRoundedBitmap(@NonNull Bitmap original, float cornerRadius) {
        ThreadStrictMode.assertBackground();

        final Bitmap output =
                Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);


        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, original.getWidth(), original.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(original, rect, rect, paint);

        return output;
    }

}
