package com.frolo.muse.engine.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.bumptech.glide.Glide;
import com.frolo.muse.R;
import com.frolo.muse.ThreadStrictMode;
import com.frolo.muse.glide.GlideAlbumArtHelper;
import com.frolo.muse.model.media.Song;

import java.util.concurrent.Future;


final class Notifications {

    private static final Bitmap EMPTY_BITMAP =
            Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);

    private static final Object sDefaultArtLock = new Object();
    private static volatile Bitmap sCachedDefaultArt = null;

    private static float dp2px(@NonNull Context context, float dp) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @WorkerThread
    @Nullable
    private static Bitmap tryGetDefaultArt(@NonNull Context context) {
        synchronized (sDefaultArtLock) {
            final Bitmap cached = sCachedDefaultArt;
            if (cached != null) {
                return cached;
            }

            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
//                options.outWidth = targetArtWidth;
//                options.outHeight = targetArtHeight;
                final Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.art_playback_notification, options);
                sCachedDefaultArt = bmp;
                return bmp;
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    private Notifications() {
    }

    @WorkerThread
    @NonNull
    public static Bitmap getPlaybackArt(@NonNull Context context, @Nullable final Song song) {
        ThreadStrictMode.assertBackground();

        final int targetArtWidth = (int) dp2px(context, 200f);
        final int targetArtHeight = (int) dp2px(context, 200f);
        final long albumId = song != null ? song.getAlbumId() : -1L;
        final Uri uri = GlideAlbumArtHelper.getUri(albumId);
        final Future<Bitmap> artFuture = Glide.with(context)
                .asBitmap()
                .override(targetArtWidth, targetArtHeight)
                .load(uri)
                .submit();

        Bitmap art = null;
        try {
            art = artFuture.get();
        } catch (Throwable ignored) {
        }

        if (art != null) {
            return art;
        }

        final Bitmap defaultArt = tryGetDefaultArt(context);

        if (defaultArt != null) {
            return defaultArt;
        }

        return EMPTY_BITMAP;
    }

}
