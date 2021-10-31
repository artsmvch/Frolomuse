package com.frolo.muse.ui.base

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.frolo.muse.*
import com.frolo.muse.model.ThemeUtils
import com.frolo.muse.repository.Preferences
import javax.inject.Inject


abstract class BaseActivity: AppCompatActivity() {
    @Inject
    lateinit var preferences: Preferences

    private var errorToast: Toast? = null

    fun requireFrolomuseApp() = application as FrolomuseApp

    override fun attachBaseContext(newBase: Context?) {
        if (Features.isLanguageChooserFeatureAvailable() && newBase != null) {
            FrolomuseApp.from(newBase).appComponent.inject(this)

            // First, get the language from the preferences
            var targetLang: String? = preferences.language
            if (targetLang.isNullOrEmpty()) {
                // Use the system language as a fallback
                targetLang = LocaleHelper.getSystemLang()
            }

            if (!targetLang.isNullOrBlank()) {
                val newLocalizedBase = LocaleHelper.applyLanguage(newBase, targetLang)
                super.attachBaseContext(newLocalizedBase)
                return
            }
        }

        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requireFrolomuseApp().appComponent.inject(this)
        applySavedTheme()
        super.onCreate(savedInstanceState)
    }

    private fun applySavedTheme() {
        val themeResId = preferences.theme?.let { theme ->
            ThemeUtils.getStyleResourceId(theme)
        }
        if (themeResId != null) {
            setTheme(themeResId)
        } else {
            if (BuildConfig.DEBUG) {
                throw IllegalStateException("Theme not found in preferences")
            }
            // Anyway we have to set up the theme
            setTheme(R.style.Base_AppTheme_Dark_Yellow)
        }
    }

    override fun onResume() {
        super.onResume()
        syncNavigationBarAppearance()
    }

    /**
     * Synchronizes the appearance of the navigation bar with its color. That is, if the color of the navigation bar
     * is light, then the appearance will be light, if the color is dark, then the appearance will be dark.
     * Calling this method is required for older versions of the API that do not support
     * the appearance attribute of the navigation bar in xml styles.
     * The best place to call this method is in the onResume callback,
     * since the window is already created at this point and the activity is in the foreground.
     */
    private fun syncNavigationBarAppearance() {
        val safeWindow: Window = this.window ?: return
        val isLight = SystemBarUtils.isLight(safeWindow.navigationBarColor)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                SystemBarUtils.setNavigationBarAppearanceLight(safeWindow, isLight)
            } catch (e: Throwable) {
                Logger.e(e)
            }
        } else {
            if (isLight) {
                // Dark navigation bar icons not supported on API level below 26,
                // set navigation bar color to black to keep icons visible.
                safeWindow.navigationBarColor = Color.BLACK
            }
        }
    }

    fun postMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun postError(error: Throwable?) {
        errorToast?.cancel()
        val msg = error?.message ?: getString(R.string.sorry_exception)
        errorToast = Toast.makeText(this, msg, Toast.LENGTH_LONG).apply { show() }
    }
}