package com.frolo.muse.ui.main

import android.content.Context
import com.frolo.muse.di.impl.navigator.AppRouterImpl
import com.frolo.muse.router.AppRouter


internal class MainAppRouterImpl(
    context: Context,
    private val host: MainFragment
): AppRouterImpl(context, host), AppRouter {

    override fun openLibrary() {
        host.switchToRoot(MainFragment.INDEX_LIBRARY)
    }

    override fun openAudioFx() {
        host.switchToRoot(MainFragment.INDEX_EQUALIZER)
    }

    override fun openSearch() {
        host.switchToRoot(MainFragment.INDEX_SEARCH)
    }

    override fun openSettings() {
        host.switchToRoot(MainFragment.INDEX_SETTINGS)
    }

    override fun openPlayer() {
        host.expandSlidingPlayer()
    }
}