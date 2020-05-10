package com.frolo.muse.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;


/**
 * BitmapDrawable that draws its bitmap cropping so that it fits the area specified by a target width and height.
 */
public class CenterCropBitmapDrawable extends BitmapDrawable {

    private final int mTargetWidth;
    private final int mTargetHeight;

    public CenterCropBitmapDrawable(Resources res, Bitmap bitmap, int targetWidth, int targetHeight) {
        super(res, bitmap);
        mTargetWidth = targetWidth;
        mTargetHeight = targetHeight;
    }

    @Override
    public void draw(Canvas canvas) {
        final Matrix drawMatrix = new Matrix();

        final int dWidth = getIntrinsicWidth();
        final int dHeight = getIntrinsicHeight();

        final int tWidth = mTargetWidth;
        final int vHeight = mTargetHeight;

        float scale;
        float dx = 0, dy = 0;
        int saveCount = canvas.getSaveCount();
        canvas.save();
        if (dWidth * vHeight > tWidth * dHeight) {
            scale = (float) vHeight / (float) dHeight;
            dx = (tWidth - dWidth * scale) * 0.5f;
        } else {
            scale = (float) tWidth / (float) dWidth;
            dy = (vHeight - dHeight * scale) * 0.5f;
        }

        drawMatrix.setScale(scale, scale);
        drawMatrix.postTranslate(Math.round(dx), Math.round(dy));
        canvas.concat(drawMatrix);

        canvas.drawBitmap(getBitmap(), 0, 0, getPaint());

        canvas.restoreToCount(saveCount);
    }

}
