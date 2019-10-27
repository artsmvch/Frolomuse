package com.frolo.muse.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public final class BitmapUtil {
    private BitmapUtil() { }

    // Worker thread
    public static Bitmap drawableToBitmap(Drawable drawable, int overrideWidth, int overrideHeight) {
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

    // Worker thread
    public static Bitmap drawableToBitmap(Drawable drawable) {
        final int width = drawable.getIntrinsicWidth();
        final int height = drawable.getIntrinsicHeight();
        return drawableToBitmap(drawable, width, height);
    }

    public static Bitmap createTransparentBitmap(int width, int height, Bitmap.Config config) {
        Bitmap newBitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.TRANSPARENT);
        return newBitmap;
    }

    private static void saveBitmap(Bitmap bmp, File file, Bitmap.CompressFormat format) throws IOException {
        final OutputStream fOut = new FileOutputStream(file);
        bmp.compress(format, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        fOut.flush(); // Not really required
        fOut.close(); // do not forget to close the stream
        //MediaStore.Images.Media.insertImage(contentResolver, file.getAbsolutePath(), file.getName(), file.getName());
    }

    public static void saveBitmapAsPng(Bitmap bmp, File file) throws IOException {
        saveBitmap(bmp, file, Bitmap.CompressFormat.PNG);
    }
}
