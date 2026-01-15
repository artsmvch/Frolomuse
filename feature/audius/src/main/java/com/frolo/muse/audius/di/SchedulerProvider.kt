package com.frolo.muse.audius.di

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

interface SchedulerProvider {
    fun worker(): Scheduler
    fun computation(): Scheduler
    fun main(): Scheduler
}

@Singleton
class AudiusSchedulerProvider @Inject constructor() : SchedulerProvider {
    override fun worker(): Scheduler = Schedulers.io()
    override fun computation(): Scheduler = Schedulers.computation()
    override fun main(): Scheduler = AndroidSchedulers.mainThread()
}
