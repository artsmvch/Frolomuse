package com.frolo.muse.glide;


import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.collection.LongSparseArray;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.frolo.muse.ThreadStrictMode;
import com.frolo.muse.arch.SingleLiveEvent;


/**
 * This is a helper class for proper loading album arts through the Glide library.
 *
 * The main problem is that Glide cannot distinguish between requests for the same {@link Uri} source.
 * For example, if we load an art for the album with id 10,
 * and after that the art gets changed in the media store
 * then any further loading of this art will return the old cached image.
 * This is why we should use {@link Key} signatures to distinguish between requests.
 *
 * NOTE: the problem is actual only if the caching is enabled.
 *
 * All the keys are stored here, in this class.
 * Keys are provided with {@link GlideAlbumArtHelper#getKey(long)} method.
 * Keys may be invalidated with {@link GlideAlbumArtHelper#invalidate(long)} method.
 *
 * All the methods should be called on {@link UiThread} because any synchronization would affect performance.
 */
@UiThread
public final class GlideAlbumArtHelper {

    //region Static helpers
    private static final Uri sAlbumArtUri;
    static {
        String path = "content://media/external/audio/albumart";
        sAlbumArtUri = Uri.parse(path);
    }

    /**
     * Creates a Uri for the given <code>albumId</code>.
     * Uri refers to the album art.
     * @param albumId for which to create a uri
     * @return uri
     */
    public static Uri getUri(long albumId) {
        return ContentUris.withAppendedId(sAlbumArtUri, albumId);
    }
    //endregion

    //region Static instance
    private static GlideAlbumArtHelper sInstance;

    /**
     * The only way to get the global instance of {@link GlideAlbumArtHelper}.
     * The instance is lazily initialized.
     *
     * Should be called on {@link UiThread}.
     *
     * @return global instance of GlideAlbumArtHelper
     */
    public static GlideAlbumArtHelper get() {
        ThreadStrictMode.assertMain();
        GlideAlbumArtHelper instance = sInstance;
        if (instance == null) {
            instance = new GlideAlbumArtHelper();
            sInstance = instance;
        }
        return instance;
    }
    //endregion

    // This is the signature keys, used by Glide to distinguish between requests to the same source.
    private LongSparseArray<ObjectKey> mKeys = new LongSparseArray<>();

    // Shared live data that notifies observers about album art changes.
    private SingleLiveEvent<Long> mAlbumArtChangedEvent = new SingleLiveEvent<>();

    private GlideAlbumArtHelper() {
    }

    /**
     * Provides a signature key for the given <code>albumId</code>.
     * First this tries to get the key from {@link GlideAlbumArtHelper#mKeys}.
     * If the key for <code>albumId</code> is not found
     * then this creates a new one and puts it in {@link GlideAlbumArtHelper#mKeys}.
     *
     * A key may be invalidated by calling {@link GlideAlbumArtHelper#invalidate(long)} method.
     *
     * Should be called on {@link UiThread}.
     *
     * @param albumId for which to provide a key
     * @return a signature key
     */
    @NonNull
    Key getKey(long albumId) {
        ThreadStrictMode.assertMain();
        ObjectKey key = mKeys.get(albumId);
        if (key == null) {
            key = new ObjectKey(new Object());
            mKeys.put(albumId, key);
        }
        return key;
    }

    /**
     * Invalidates the key for the given <code>albumId</code> by simply creating a new one
     * and putting it in {@link GlideAlbumArtHelper#mKeys}.
     * This also emits an event to {@link GlideAlbumArtHelper#mAlbumArtChangedEvent}
     * letting observers know that the album art with the id <code>albumId</code> has changed.
     *
     * Should be called on {@link UiThread}.
     *
     * @param albumId for which to invalidate the key
     */
    public void invalidate(long albumId) {
        ThreadStrictMode.assertMain();
        ObjectKey newKey = new ObjectKey(new Object());
        mKeys.put(albumId, newKey);
        mAlbumArtChangedEvent.setValue(albumId);
    }

    /**
     * Makes a request options for the given <code>albumId</code>.
     * The main is that the options are signed with a special key for the given <code>albumId</code>.
     * The key is valid until {@link GlideAlbumArtHelper#invalidate(long)} is called for the given <code>albumId</code>.
     * Also, the memory cache is not skipped and no disk cache strategy is set.
     *
     * Should be called on {@link UiThread}.
     *
     * @param albumId for which to make RequestOptions
     * @return RequestOptions
     */
    @NonNull
    public RequestOptions makeRequestOptions(long albumId) {
        ThreadStrictMode.assertMain();
        final Key key = getKey(albumId);
        return new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(false)
                .signature(key);
    }

    /**
     * Makes a request for the given <code>albumId</code>.
     * The request loads Uri for album art (@see {@link GlideAlbumArtHelper#getUri(long)}).
     * Request options made with {@link GlideAlbumArtHelper#makeRequestOptions(long)} are applied to the request.
     *
     * Should be called on {@link UiThread}.
     *
     * @param manager to make RequestBuilder by loading the model
     * @param albumId for which to make RequestBuilder
     * @return RequestBuilder
     */
    @NonNull
    public RequestBuilder<Drawable> makeRequest(
        @NonNull RequestManager manager,
        long albumId
    ) {
        ThreadStrictMode.assertMain();
        final Uri model = getUri(albumId);
        final RequestOptions options = makeRequestOptions(albumId);
        return manager.load(model).apply(options);
    }

    /**
     * Makes a request as Bitmap for the given <code>albumId</code>.
     * The request loads Uri for album art (@see {@link GlideAlbumArtHelper#getUri(long)}).
     * Request options made with {@link GlideAlbumArtHelper#makeRequestOptions(long)} are applied to the request.
     *
     * Should be called on {@link UiThread}.
     *
     * @param manager to make RequestBuilder by loading the model
     * @param albumId for which to make RequestBuilder
     * @return RequestBuilder
     */
    @NonNull
    public RequestBuilder<Bitmap> makeRequestAsBitmap(
        @NonNull RequestManager manager,
        long albumId
    ) {
        ThreadStrictMode.assertMain();
        final Uri model = getUri(albumId);
        final RequestOptions options = makeRequestOptions(albumId);
        return manager.asBitmap().load(model).apply(options);
    }

    /**
     * Observes all album art changes that are dispatched through {@link GlideAlbumArtHelper#invalidate(long)} method.
     * @param owner lifecycle owner
     * @param observer callback
     */
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<Long> observer) {
        ThreadStrictMode.assertMain();
        mAlbumArtChangedEvent.observe(owner, observer);
    }

}
