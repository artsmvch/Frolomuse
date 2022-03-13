package com.frolo.muse.rx

import io.reactivex.Scheduler


interface SchedulerProvider {
    /**
     * Scheduler for long running blocking operations, typically for I/O-bound work.
     */
    fun worker(): Scheduler

    /**
     * Scheduler for computational work.
     */
    fun computation(): Scheduler

    /**
     * Main thread scheduler.
     */
    fun main(): Scheduler
}