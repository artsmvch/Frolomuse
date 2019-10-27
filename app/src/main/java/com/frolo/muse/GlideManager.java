package com.frolo.muse;


import android.content.ContentUris;
import android.net.Uri;

import androidx.annotation.UiThread;
import androidx.collection.LongSparseArray;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;


// TO-DO: i don't think it's a good idea to hold image keys in a singleton
// NOTE: not thread-safe. All operations should be performed on main thread.
@UiThread
public final class GlideManager {

    private static GlideManager instance;

    private static final String PATH = "content://media/external/audio/albumart";
    private static final Uri ALBUM_URI = Uri.parse(PATH);

    private final LongSparseArray<Object> keys = new LongSparseArray<>();
    private final RequestOptions defaultRequestOptions = new RequestOptions()
            .skipMemoryCache(false)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.ic_note_rounded_placeholder)
            .error(R.drawable.ic_note_rounded_placeholder)
            .lock();

    public static Uri albumArtUri(long albumId) {
        return ContentUris.withAppendedId(albumArtUri(), albumId);
    }

    public static Uri albumArtUri() {
        return ALBUM_URI;
    }

    public static GlideManager get() {
        ThreadStrictMode.assertMain();
        if (instance == null) {
            instance = new GlideManager();
        }
        return instance;
    }

    public /*non-null*/ Object invalidateKey(long albumId) {
        ThreadStrictMode.assertMain();
        Object key = new Object();
        keys.put(albumId, key);
        return key;
    }

    public RequestOptions defaultRequestOptions() {
        return defaultRequestOptions.clone();
    }

    public RequestOptions requestOptions(long albumId) {
        ThreadStrictMode.assertMain();
        Object key = keys.get(albumId);
        if (key == null) {
            key = invalidateKey(albumId);
        }
        return defaultRequestOptions.clone()
                .signature(new ObjectKey(key));
    }

}
