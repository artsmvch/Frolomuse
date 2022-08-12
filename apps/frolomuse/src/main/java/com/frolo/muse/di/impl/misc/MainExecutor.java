package com.frolo.muse.di.impl.misc;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class MainExecutor implements Executor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void execute(@NotNull Runnable runnable) {
        handler.post(runnable);
    }
}
