package com.frolo.muse.router

import android.os.Handler


/**
 * Delegates actions to [appRouter] on the thread defined by [handler].
 */
class ThreadAppRouterWrapper(
    private val appRouter: AppRouter,
    private val handler: Handler
): AppRouterDelegate() {

    override fun delegate(action: (AppRouter) -> Unit) {
        val task = Runnable { action.invoke(appRouter) }
        handler.post(task)
    }

}