package com.frolo.muse.rx

import io.reactivex.Scheduler

interface SchedulerProvider {
    fun worker(): Scheduler

    fun computation(): Scheduler

    fun main(): Scheduler
}