package com.frolo.muse.rx

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory


fun newSingleThreadScheduler(threadName: String? = null): Scheduler {
    val executor = newSingleThreadExecutor(threadName)
    return Schedulers.from(executor)
}

fun newSingleThreadExecutor(threadName: String? = null): Executor {
    val threadFactory = ThreadFactory { runnable ->
        Thread(runnable).apply {
            if (!threadName.isNullOrBlank()) {
                name = threadName
            }
        }
    }
    return Executors.newSingleThreadExecutor(threadFactory)
}