package com.frolo.rx.flowable

import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.BiConsumer
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Transforms [this] source so that it emits items with their emission index.
 */
fun <T> Flowable<T>.indexed(): Flowable<IndexedValue<T>> {
    return RxJavaPlugins.onAssembly(FlowableIndexedValue(this))
}

/**
 * Similar to [Flowable.doOnNext] but also gives the emission index for each item.
 */
fun <T> Flowable<T>.doOnNextIndexed(consumer: BiConsumer<Int, T>): Flowable<T> {
    return indexed()
        .doOnNext { indexedValue ->
            consumer.accept(indexedValue.index, indexedValue.value)
        }
        .map { indexedValue -> indexedValue.value }
}

/**
 * Similar to [Flowable.doOnNext] but also gives the emission index for each item.
 */
fun <T> Flowable<T>.doOnNextIndexed(consumer: (index: Int, item: T) -> Unit): Flowable<T> {
    return doOnNextIndexed(BiConsumer(consumer))
}

/**
 * Fires [consumer] on the first item emitted.
 */
fun <T> Flowable<T>.doOnFirst(consumer: Consumer<T>): Flowable<T> {
    return doOnNextIndexed { index, item ->
        if (index == 0) consumer.accept(item)
    }
}

/**
 * Fires [consumer] on the first item emitted.
 */
fun <T> Flowable<T>.doOnFirst(consumer: (T) -> Unit): Flowable<T> {
    return doOnFirst(Consumer(consumer))
}

/**
 * Emits default [item] after the specified [delay] if there are no items emitted by [this] source yet.
 */
fun <T> Flowable<T>.withDefaultItemDelayed(item: T, delay: Long, unit: TimeUnit, scheduler: Scheduler = Schedulers.computation()): Flowable<T> {
    return RxJavaPlugins.onAssembly(FlowableWithDefaultItemDelayed(this, item, delay, unit, scheduler))
}

/**
 * Sets a timeout for the first element only.
 */
fun <T> Flowable<T>.timeoutForFirstElement(timeout: Long, timeUnit: TimeUnit): Flowable<T> {
    return timeout(Flowable.timer(timeout, timeUnit), Function { _: T -> Flowable.never<Unit>() })
}