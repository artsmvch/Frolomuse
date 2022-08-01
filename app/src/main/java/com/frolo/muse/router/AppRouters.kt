@file:Suppress("FunctionName")

package com.frolo.muse.router

import com.frolo.core.ui.ActivityWatcher


fun AppRouter(activityWatcher: ActivityWatcher): AppRouter {
    return AppRouterDelegate {
        when (val activity = activityWatcher.foregroundActivity) {
            is AppRouter -> activity
            is AppRouter.Provider -> activity.getRouter()
            else -> null
        }
    }
}

fun AppRouterStub(): AppRouter {
    return AppRouterDelegate { null }
}