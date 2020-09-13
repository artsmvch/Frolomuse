package com.frolo.muse.glide;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;


public abstract class SimpleRequestListener <R> implements RequestListener<R> {

    @Override
    public final boolean onLoadFailed(@Nullable GlideException e, Object model, Target<R> target, boolean isFirstResource) {
        doWhenFailed(e);
        return false;
    }

    @Override
    public final boolean onResourceReady(R resource, Object model, Target<R> target, DataSource dataSource, boolean isFirstResource) {
        doWhenReady(resource);
        return false;
    }

    public void doWhenFailed(@Nullable GlideException e) {
    }

    public void doWhenReady(R resource) {
    }

}
