package com.frolo.muse.ui.main.settings.info

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.frolo.debug.DebugUtils
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.databinding.DialogAppInfoBinding
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logEasterEggFound
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.ui.StyleUtils


class AppInfoDialog : BaseDialogFragment() {

    private val eventLogger: EventLogger by eventLogger()

    private lateinit var anim: Animation

    /**
     * The count of clicks on the app icon.
     */
    private var appIconClickCount: Int = 0

    private var fullVersionShown: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        anim = AnimationUtils.loadAnimation(context, R.anim.rotation_overshot).apply {
            interpolator = OvershootInterpolator()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadUi(this)
        }
    }

    override fun onDestroy() {
        eventLogger.logEasterEggFound(clickCount = appIconClickCount)
        super.onDestroy()
    }

    private fun loadUi(dialog: Dialog) = with(dialog) {
        val binding = DialogAppInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tsVersion.apply {
            setFactory { TextView(context) }
            setInAnimation(context, R.anim.fade_in)
            setOutAnimation(context, R.anim.fade_out)
            setText(BuildConfig.VERSION_NAME)
        }

        binding.imvAppIcon.setOnClickListener { view ->
            appIconClickCount++
            view.startAnimation(anim)
            maybeShowFullBuildVersion(binding.tsVersion, binding.imvFirebaseLogo)
        }
    }

    private fun maybeShowFullBuildVersion(
        tsVersion: TextSwitcher,
        fbLogo: ImageView
    ) = dialog?.apply {
        if (appIconClickCount >= 15 && !fullVersionShown) {
            val fullBuildVersion = "${BuildConfig.VERSION_NAME}(${BuildConfig.BUILD_SCRIPT_TIME})"
            tsVersion.setText(fullBuildVersion)

            val isLightTheme = try {
                StyleUtils.resolveBool(this.context, com.google.android.material.R.attr.isLightTheme)
            } catch (e: Throwable) {
                DebugUtils.dumpOnMainThread(e)
                false
            }
            @DrawableRes val logoResId: Int =
                if (isLightTheme) R.drawable.ic_firebase_logo_light
                else R.drawable.ic_firebase_logo_dark
            fbLogo.setImageResource(logoResId)
            fbLogo.isVisible = BuildConfig.GOOGLE_SERVICES_ENABLED

            fullVersionShown = true
        }
    }

    companion object {

        // Factory
        fun newInstance(): AppInfoDialog = AppInfoDialog()

    }

}
