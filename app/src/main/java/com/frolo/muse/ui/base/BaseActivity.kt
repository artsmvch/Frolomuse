package com.frolo.muse.ui.base

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.frolo.muse.FrolomuseApp
import com.frolo.muse.R
import com.frolo.muse.model.Theme
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
        when(preferences.theme) {
            Theme.LIGHT_BLUE -> setTheme(R.style.AppTheme_Light_Blue)
            Theme.DARK_BLUE -> setTheme(R.style.Base_AppTheme_Dark_Blue)
            Theme.DARK_BLUE_ESPECIAL -> setTheme(R.style.Base_AppTheme_Dark_Blue_Especial)
            Theme.DARK_PURPLE -> setTheme(R.style.Base_AppTheme_Dark_Purple)
            Theme.DARK_ORANGE -> setTheme(R.style.Base_AppTheme_Dark_Yellow)
            Theme.DARK_GREEN -> setTheme(R.style.Base_AppTheme_Dark_Green)
            else -> setTheme(R.style.Base_AppTheme_Dark_Yellow) // anyway we have to set up any theme
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