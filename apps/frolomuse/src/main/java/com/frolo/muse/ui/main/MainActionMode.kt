package com.frolo.muse.ui.main

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ActionMode
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.frolo.core.ui.actionmode.ActionModeView
import com.frolo.core.ui.systembars.SystemBarsControlOwner
import com.frolo.core.ui.systembars.SystemBarsController
import com.frolo.core.ui.systembars.SystemBarsHost
import com.frolo.debug.DebugUtils
import com.frolo.ui.SystemBarUtils


internal class MainActionMode(
    private val delegate: AppCompatDelegate,
    private val contextView: ActionModeView,
    private val systemBarsHost: SystemBarsHost?,
    private val callback: ActionMode.Callback
): ActionMode() {

    private val context: Context get() = contextView.context

    private var isOrWillBeFinished: Boolean = false

    private val systemBarsControlOwner = object : SystemBarsControlOwner {
        override fun onSystemBarsControlObtained(controller: SystemBarsController) {
            // All themes have dark background for action mode
            controller.setStatusBarAppearanceLight(isLight = false)
        }
    }

    fun createAndShow() {
        contextView.menu.clear()
        contextView.setOnClickListener { /* stub */ }
        contextView.setOnCloseClickListener { finish() }
        contextView.setOnMenuItemClickListener { callback.onActionItemClicked(this, it) }
        ViewCompat.setOnApplyWindowInsetsListener(contextView) { view, insets ->
            view.updatePadding(top = insets.systemWindowInsetTop)
            insets
        }
        ViewCompat.requestApplyInsets(contextView)
        callback.onCreateActionMode(this, contextView.menu)
        systemBarsHost?.obtainSystemBarsControl(systemBarsControlOwner)
        contextView.show()
        isOrWillBeFinished = false
    }

    private fun hide() {
        systemBarsHost?.abandonSystemBarsControl(systemBarsControlOwner)
        contextView.hide()
    }

    override fun setTitle(title: CharSequence?) {
        contextView.title = title
    }

    override fun setTitle(@StringRes resId: Int) {
        contextView.title = context.resources.getString(resId)
    }

    override fun setSubtitle(subtitle: CharSequence?) {
        contextView.subtitle = subtitle
    }

    override fun setSubtitle(@StringRes resId: Int) {
        contextView.subtitle = context.resources.getString(resId)
    }

    override fun setCustomView(view: View?) {
        DebugUtils.dumpOnMainThread(IllegalStateException("Custom view not supported"))
    }

    override fun invalidate() {
        callback.onPrepareActionMode(this, contextView.menu)
    }

    override fun finish() {
        if (isOrWillBeFinished) {
            return
        }
        isOrWillBeFinished = true
        contextView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        callback.onDestroyActionMode(this)
        hide()
    }

    override fun getMenu(): Menu = contextView.menu

    override fun getTitle(): CharSequence = contextView.title ?: ""

    override fun getSubtitle(): CharSequence = contextView.subtitle ?: ""

    override fun getCustomView(): View? = null

    override fun getMenuInflater(): MenuInflater {
        // Use delegate to access support menu inflater
        return delegate.menuInflater ?: MenuInflater(context)
    }
}