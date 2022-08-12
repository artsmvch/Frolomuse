package com.frolo.muse.rx

import io.reactivex.*
import io.reactivex.disposables.Disposable


fun Completable.subscribeSafely(): Disposable = subscribe({}, {})

fun Maybe<*>.subscribeSafely(): Disposable = subscribe({}, {})

fun Single<*>.subscribeSafely(): Disposable = subscribe({}, {})

fun Observable<*>.subscribeSafely(): Disposable = subscribe({}, {})

fun Flowable<*>.subscribeSafely(): Disposable = subscribe({}, {})