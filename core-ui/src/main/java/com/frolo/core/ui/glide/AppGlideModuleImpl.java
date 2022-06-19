package com.frolo.core.ui.glide;

import android.app.ActivityManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.load.model.UriLoader;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.frolo.debug.DebugUtils;

import java.io.InputStream;


@GlideModule
public final class AppGlideModuleImpl extends AppGlideModule {
    private final static String LOG_TAG = "AppGlideModuleImpl";

    /**
     * Having a lot of OOM errors, gotta blame Glide for this.
     * Let's try reducing the size of the memory cache.
     * @param context context for checking the device spec
     * @return memory cache size, in bytes
     */
    private static int calculateMemoryCacheSize(@NonNull Context context) {
        ActivityManager manager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            DebugUtils.dumpOnMainThread(new NullPointerException("No ActivityManager found"));
            return 0;
        }

        // Calculated by Glide
        final int calculatedSize = new MemorySizeCalculator.Builder(context)
            .build()
            .getMemoryCacheSize();

        // The max size we can afford
        final int maxSize;
        if (manager.isLowRamDevice()) {
            // 2 megabytes
            maxSize = 2 * 1024 * 1024;
        } else {
            // 6 megabytes
            maxSize = 6 * 1024 * 1024;
        }

        return Math.min(calculatedSize, maxSize);
    }

    private final RequestOptions defaultRequestOptions =
        new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(false);

    @Override
    public boolean isManifestParsingEnabled() {
        // Return false, cause there were no migration from v3. See docs.
        return false;
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        Log.d(LOG_TAG, "Register components...");
        // It is important to replace the default ModelLoaderFactory for Uri models.
        // By default, Glide loads thumbnails instead of original arts for Uris if the requested size is small.
        // The thumbnails differ from the original images and this may confuse users.
        // The usual case is the album arts in the Player screen (big image size) and list of songs (small image size).
        // So the correct solution is to load only the original album art by its Uri.
        registry.replace(Uri.class, InputStream.class, new UriLoader.StreamFactory(context.getContentResolver()));
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        Log.d(LOG_TAG, "Apply options...");
        // Default options
        builder.setDefaultRequestOptions(defaultRequestOptions);

        // Memory cache
        int memoryCacheSize = calculateMemoryCacheSize(context);
        builder.setMemoryCache(new LruResourceCache(memoryCacheSize));
    }
}
