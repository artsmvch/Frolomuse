package com.frolo.muse.rx

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory


fun newSingleThreadScheduler(threadName: String? = null): Scheduler {
    val threadFactory = ThreadFactory { runnable ->
        Thread(runnable).apply {
            if (!threadName.isNullOrBlank()) {
                name = threadName
            }
        }
    }
    val executor = Executors.newSingleThreadExecutor(threadFactory)
    return Schedulers.from(executor)
}