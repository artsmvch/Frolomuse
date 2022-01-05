package com.frolo.muse.thumbnails;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;

import com.frolo.debug.DebugUtils;


final class DrawableTransformationCache {

    private final LruCache<Context, LruCache<Integer, Drawable>> mCache =
            new LruCache<Context, LruCache<Integer, Drawable>>(2) {
        @Override
        protected LruCache<Integer, Drawable> create(@NonNull Context key) {
            return new LruCache<>(10);
        }
    };

    @Nullable
    Drawable get(@NonNull Context context, @DrawableRes int drawableResId) {
        LruCache<Integer, Drawable> innerCache = mCache.get(context);
        if (innerCache == null) {
            DebugUtils.dumpOnMainThread(new NullPointerException());
            return null;
        }
        return innerCache.get(drawableResId);
    }

    void put(@NonNull Context context, @DrawableRes int drawableResId, @NonNull Drawable value) {
        LruCache<Integer, Drawable> innerCache = mCache.get(context);
        if (innerCache == null) {
            DebugUtils.dumpOnMainThread(new NullPointerException());
            return;
        }
        innerCache.put(drawableResId, value);
    }
}
