package com.frolo.muse.glide;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.UriLoader;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

import java.io.InputStream;


@GlideModule
public final class AppGlideModuleImpl extends AppGlideModule {

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
        // It is important to replace the default ModelLoaderFactory for Uri models.
        // By default, Glide loads thumbnails instead of original arts for Uris if the requested size is small.
        // The thumbnails differ from the original images and this may confuse users.
        // The usual case is the album arts in the Player screen (big image size) and list of songs (small image size).
        // So the correct solution is to load only the original album art by its Uri.
        registry.replace(Uri.class, InputStream.class, new UriLoader.StreamFactory(context.getContentResolver()));
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setDefaultRequestOptions(defaultRequestOptions);
    }
}
