package com.frolo.muse.di.impl.local;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


final class ExecutorHolder {

    private static class WorkerExecutor {
        static final Executor sInstance;

        static {
            ThreadFactory factory = new ThreadFactory() {

                final AtomicLong threadId = new AtomicLong(0);

                @Override
                public Thread newThread(Runnable r) {
                    String name = "ContentQueryExecutor-" + threadId.getAndIncrement();
                    return new Thread(r, name);
                }
            };
            sInstance = Executors.newCachedThreadPool(factory);
        }
    }

    static Executor workerExecutor() {
        return WorkerExecutor.sInstance;
    }

    static Scheduler workerScheduler() {
        return Schedulers.from(workerExecutor());
    }

    private ExecutorHolder() {
    }
}
