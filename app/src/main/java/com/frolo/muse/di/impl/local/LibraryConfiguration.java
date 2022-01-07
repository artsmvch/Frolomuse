package com.frolo.muse.di.impl.local;

import android.content.Context;

import androidx.annotation.NonNull;

import com.frolo.music.repository.SongFilterProvider;

import java.util.concurrent.Executor;


public final class LibraryConfiguration {
    @NonNull
    private final Context mContext;
    @NonNull
    private final SongFilterProvider mSongFilterProvider;
    @NonNull
    private final Executor mQueryExecutor;

    public LibraryConfiguration(
            @NonNull Context context,
            @NonNull SongFilterProvider songFilterProvider,
            @NonNull Executor queryExecutor) {
        mContext = context;
        mSongFilterProvider = songFilterProvider;
        mQueryExecutor = queryExecutor;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public SongFilterProvider getSongFilterProvider() {
        return mSongFilterProvider;
    }

    @NonNull
    public Executor getQueryExecutor() {
        return mQueryExecutor;
    }
}
