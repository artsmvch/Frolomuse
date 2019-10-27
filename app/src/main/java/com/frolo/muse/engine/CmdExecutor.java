package com.frolo.muse.engine;

import androidx.annotation.Nullable;


/*Single-threaded command executor*/
public interface CmdExecutor {
    boolean isOnThread();

    void exec(Runnable cmd);

    void post(@Nullable Object token, Runnable cmd);

    void shutdown();
}
