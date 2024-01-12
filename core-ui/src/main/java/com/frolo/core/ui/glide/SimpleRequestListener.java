package com.frolo.core.ui.glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;


public abstract class SimpleRequestListener <R> implements RequestListener<R> {

    @Override
    public final boolean onLoadFailed(
            @Nullable GlideException e, Object model, @NonNull Target<R> target, boolean isFirstResource) {
        doWhenFailed(e);
        return false;
    }

    @Override
    public final boolean onResourceReady(
            @NonNull R resource, @NonNull Object model, Target<R> target, @NonNull DataSource dataSource, boolean isFirstResource) {
        doWhenReady(resource);
        return false;
    }

    public void doWhenFailed(@Nullable GlideException e) {
    }

    public void doWhenReady(R resource) {
    }

}
