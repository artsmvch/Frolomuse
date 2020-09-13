package com.frolo.muse.rx

import android.app.Service
import androidx.annotation.CallSuper
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Rx-adapted service, which contains convenient methods for working with Rx disposables and subscriptions.
 * Disposables can be saved via [save] method for automatic disposal when the service is destroyed.
 */
abstract class RxService : Service() {

    private val isDestroyed = AtomicBoolean(false)

    private val disposables = CompositeDisposable()

    @CallSuper
    override fun onDestroy() {
        isDestroyed.set(true)
        disposables.clear()
        super.onDestroy()
    }

    protected fun Completable.subscribeSafely() {
        subscribe({ /* stub */ }, { /* stub */ }).save()
    }

    /**
     * Saves [this] disposable for disposal when the service is destroyed.
     * If the service has already been destroyed, then the disposable will be immediately disposed.
     */
    protected fun Disposable.save() {
        disposables.add(this)
        if (isDestroyed.get()) {
            dispose()
        }
    }

}