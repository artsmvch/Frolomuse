package com.frolo.muse.di.impl.local;

import androidx.annotation.NonNull;

import com.frolo.muse.DebugUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
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

        private static final AtomicLong sThreadId = new AtomicLong(0);
        static final ThreadFactory sThreadFactory = r -> {
            String name = "ContentWorker-" + sThreadId.getAndIncrement();
            return new Thread(r, name);
        };

        static final RejectedExecutionHandler sRejectedExecutionHandler = (r, executor) -> {
            RejectedExecutionException exception = new RejectedExecutionException(
                    "Task " + r.toString() + " rejected from " + executor.toString());
            DebugUtils.dumpOnMainThread(exception);
            throw exception;
        };

        static final Executor sInstance;

        static {
            sInstance = createInstance0();
        }

        private static Executor createInstance0() {
            // allow core thread timeout to timeout the core threads so that
            // when no tasks arrive, the core threads can be killed
            ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_THREAD_NUMBER, MAX_THREAD_NUMBER,
                    KEEP_ALIVE_TIME_IN_SECONDS, TimeUnit.SECONDS,
                    new LinkedBlockingDeque<Runnable>() /* unbounded queue*/,
                    sThreadFactory, sRejectedExecutionHandler);
            executor.allowCoreThreadTimeOut(true);
            return executor;
        }

//        private static Executor createInstance0() {
//            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_NUMBER, sThreadFactory);
//            if (executor instanceof ThreadPoolExecutor) {
//                // Be careful with this shit... It may crash
//                ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
//                pool.setCorePoolSize(CORE_THREAD_NUMBER);
//                pool.setKeepAliveTime(KEEP_ALIVE_TIME_IN_SECONDS, TimeUnit.SECONDS);
//                pool.allowCoreThreadTimeOut(true);
//                pool.setRejectedExecutionHandler(sRejectedExecutionHandler);
//            }
//            return executor;
//        }

    }

    private static class WorkerSchedulerHolder {
        static final Scheduler sInstance = Schedulers.from(WorkerExecutorHolder.sInstance);
    }

    /**
     * Worker executor for content queries. It doesn't matter if the operations are heavy or light,
     * they should be used for IO tasks. The executor is based on a thread pool and is lazy initialized.
     * @return worker executor
     */
    @NonNull
    static Executor workerExecutor() {
        return WorkerExecutorHolder.sInstance;
    }

    /**
     * Default worker scheduler based on the worker executor and used for query operations.
     * @return worker scheduler
     */
    @NonNull
    static Scheduler workerScheduler() {
        return WorkerSchedulerHolder.sInstance;
    }

    /**
     * Used for several small tasks executed in parallel. The default work scheduler is currently returned.
     * @return fast parallel scheduler
     */
    @NonNull
    static Scheduler fastParallelScheduler() {
        return workerScheduler();
    }

    @NonNull
    static Scheduler computationScheduler() {
        return Schedulers.computation();
    }

    private ContentExecutors() {
    }
}
