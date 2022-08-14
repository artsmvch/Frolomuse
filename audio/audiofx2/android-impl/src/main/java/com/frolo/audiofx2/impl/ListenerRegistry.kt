package com.frolo.audiofx2.impl

import android.content.Context
import android.os.Handler
import androidx.annotation.GuardedBy

internal abstract class ListenerRegistry<LISTENER>(
    private val context: Context
) {
    private val handler = Handler(context.mainLooper)
    private val listenersLock = Any()
    @GuardedBy("listenersLock")
    private val listeners = ArrayList<LISTENER>(3)

    fun addListener(listener: LISTENER) {
        synchronized(listenersLock) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: LISTENER) {
        synchronized(listenersLock) {
            listeners.remove(listener)
        }
    }

    protected fun doDispatch(action: (LISTENER) -> Unit) {
        handler.post {
            synchronized(listeners) {
                listeners.forEach { listener ->
                    action.invoke(listener)
                }
            }
        }
    }
}