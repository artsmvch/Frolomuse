package com.frolo.muse.ui.base


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.frolo.muse.App
import com.frolo.muse.R
import com.frolo.muse.mediascan.MediaScanService
import com.frolo.muse.repository.Preferences
import javax.inject.Inject


abstract class BaseActivity: AppCompatActivity() {
    @Inject
    lateinit var preferences: Preferences

    private var errorToast: Toast? = null

    fun requireApp() = application as App

    private val scanningStatusIntentFilter = IntentFilter(MediaScanService.ACTION_MEDIA_SCANNING_STATUS)
    private val scanningStatusBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            val action = intent?.action
            if (action == MediaScanService.ACTION_MEDIA_SCANNING_STATUS) {
                if (intent.getBooleanExtra(MediaScanService.EXTRA_MEDIA_SCANNING_STARTED, false)) {
                    postMessage(getString(R.string.scanning_started))
                } else if (intent.getBooleanExtra(MediaScanService.EXTRA_MEDIA_SCANNING_COMPLETED, false)) {
                    postMessage(getString(R.string.scanning_completed))
                } else if (intent.getBooleanExtra(MediaScanService.EXTRA_MEDIA_SCANNING_CANCELLED, false)) {
                    // User cancelled the scanning himself, no need to notify him about that
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requireApp().appComponent.inject(this)
        applySavedTheme()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(scanningStatusBroadcastReceiver, scanningStatusIntentFilter)
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(scanningStatusBroadcastReceiver)
        super.onStop()
    }

    private fun applySavedTheme() {
        when(preferences.theme) {
            Preferences.THEME_LIGHT -> setTheme(R.style.Base_AppTheme_Light)
            Preferences.THEME_DARK_BLUE -> setTheme(R.style.Base_AppTheme_Dark_Blue)
            Preferences.THEME_DARK_BLUE_ESPECIAL -> setTheme(R.style.Base_AppTheme_Dark_Blue_Especial)
            Preferences.THEME_DARK_PURPLE -> setTheme(R.style.Base_AppTheme_Dark_Purple)
            else -> setTheme(R.style.Base_AppTheme_Dark_Blue) // anyway we have to set up any theme
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