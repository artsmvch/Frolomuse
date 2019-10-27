package com.frolo.muse.engine.service;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.frolo.muse.engine.CmdExecutor;


public class HandlerCmdExecutor implements CmdExecutor {
    private final Handler mHandler;

    public HandlerCmdExecutor(@NonNull Handler handler) {
        this.mHandler = handler;
    }

    @Override
    public boolean isOnThread() {
        return Thread.currentThread() == mHandler.getLooper().getThread();
    }

    @Override
    public void exec(Runnable cmd) {
        if (isOnThread()) {
            cmd.run();
        } else {
            mHandler.post(cmd);
        }
    }

    @Override
    public void post(@Nullable Object token, Runnable cmd) {
        if (token != null) {
            mHandler.removeCallbacksAndMessages(token);
            mHandler.postAtTime(cmd, token, 0);
        } else {
            mHandler.post(cmd);
        }
    }

    @Override
    public void shutdown() {
        mHandler.removeCallbacksAndMessages(null);
    }
}
