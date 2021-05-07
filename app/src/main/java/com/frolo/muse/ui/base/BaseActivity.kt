package com.frolo.muse.ui.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.frolo.muse.BuildConfig
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.R
import com.frolo.muse.model.ThemeUtils
import com.frolo.muse.repository.Preferences
import javax.inject.Inject


abstract class BaseActivity: AppCompatActivity() {
    @Inject
    lateinit var preferences: Preferences

    private var errorToast: Toast? = null

    fun requireFrolomuseApp() = application as FrolomuseApp

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

    fun postMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun postError(error: Throwable?) {
        errorToast?.cancel()
        val msg = error?.message ?: getString(R.string.sorry_exception)
        errorToast = Toast.makeText(this, msg, Toast.LENGTH_LONG).apply { show() }
    }
}