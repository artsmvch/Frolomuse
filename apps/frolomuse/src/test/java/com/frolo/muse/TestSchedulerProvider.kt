package com.frolo.muse

import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers


class TestSchedulerProvider private constructor(): SchedulerProvider {

    companion object {
        val SHARED by lazy { TestSchedulerProvider() }
    }

    private val scheduler = Schedulers.from { it.run() }

    override fun worker(): Scheduler = scheduler

    override fun computation(): Scheduler  = scheduler

    override fun main(): Scheduler = scheduler
}