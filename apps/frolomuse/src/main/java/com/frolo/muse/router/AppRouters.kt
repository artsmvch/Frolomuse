@file:Suppress("FunctionName")

package com.frolo.muse.router

import com.frolo.core.ui.ApplicationWatcher


fun AppRouter(): AppRouter {
    return AppRouterDelegate {
        when (val activity = ApplicationWatcher.foregroundActivity) {
            is AppRouter -> activity
            is AppRouter.Provider -> activity.getRouter()
            else -> null
        }
    }
}

fun AppRouterStub(): AppRouter {
    return AppRouterDelegate { null }
}