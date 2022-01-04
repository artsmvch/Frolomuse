package com.frolo.muse.router

import java.util.concurrent.atomic.AtomicReference


/**
 * Mutable app router wrapper, that is, the wrapped delegate can be set and unset
 * using the corresponding [attachBase] and [detachBase] methods.
 */
class MutableAppRouterWrapper : AppRouterDelegate() {

    private val delegateRef = AtomicReference<AppRouter>()

    fun attachBase(router: AppRouter) {
        delegateRef.set(router)
    }

    fun detachBase() {
        delegateRef.set(null)
    }

    override fun delegate(action: (AppRouter) -> Unit) {
        delegateRef.get()?.also(action)
    }
}