package com.frolo.muse.di.impl.rx

import com.frolo.muse.rx.SchedulerProvider
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors


class SchedulerProviderImpl : SchedulerProvider {
    private val workerExecutor = Executors.newFixedThreadPool(3)
    private val workerScheduler = Schedulers.from(workerExecutor)

    override fun worker(): Scheduler = workerScheduler

    override fun computation(): Scheduler = Schedulers.computation()

    override fun main(): Scheduler = AndroidSchedulers.mainThread()

}