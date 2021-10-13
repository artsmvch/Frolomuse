package com.frolo.muse.di.impl.local;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


/* package-private */ final class ContentExecutors {

    private static class WorkerExecutorHolder {

        // Configuration
        static final int CORE_THREAD_NUMBER = 8;
        static final int MAX_THREAD_NUMBER = 32;
        static final long KEEP_ALIVE_TIME_IN_SECONDS = 10L;

        static final Executor sInstance;

        static {
            ThreadFactory threadFactory = new ThreadFactory() {
                final AtomicLong threadId = new AtomicLong(0);

                @Override
                public Thread newThread(Runnable r) {
                    String name = "ContentWorker-" + threadId.getAndIncrement();
                    return new Thread(r, name);
                }
            };
            sInstance = new ThreadPoolExecutor(CORE_THREAD_NUMBER, MAX_THREAD_NUMBER,
                    KEEP_ALIVE_TIME_IN_SECONDS, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(),
                    threadFactory);
        }
    }

    private static class WorkerSchedulerHolder {
        static final Scheduler sInstance = Schedulers.from(WorkerExecutorHolder.sInstance);
    }

    @NonNull
    static Executor workerExecutor() {
        return WorkerExecutorHolder.sInstance;
    }

    @NonNull
    static Scheduler workerScheduler() {
        return WorkerSchedulerHolder.sInstance;
    }

    private ContentExecutors() {
    }
}
