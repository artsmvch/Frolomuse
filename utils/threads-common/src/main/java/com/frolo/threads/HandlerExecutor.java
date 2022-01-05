package com.frolo.threads;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;


public class HandlerExecutor implements Executor {
    @NonNull
    private final Handler mHandler;

    public HandlerExecutor(@NonNull Handler handler) {
        mHandler = handler;
    }

    public HandlerExecutor(@NonNull Looper looper) {
        this(new Handler(looper));
    }

    @Override
    public void execute(Runnable command) {
        mHandler.post(command);
    }
}
