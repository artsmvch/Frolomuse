package com.frolo.muse.ui.main.settings.info

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import com.frolo.muse.logger.EventLogger
import com.frolo.muse.logger.logEasterEggFound
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_app_info.*


class AppInfoDialog : BaseDialogFragment() {

    private val eventLogger: EventLogger by eventLogger()

    private lateinit var anim: Animation

    /**
     * The count of clicks on the app icon.
     */
    private var appIconClickCount: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        anim = AnimationUtils.loadAnimation(context, R.anim.rotation_overshot).apply {
            interpolator = OvershootInterpolator()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_app_info)
            loadUI(this)
        }
    }

    override fun onDestroy() {
        eventLogger.logEasterEggFound(clickCount = appIconClickCount)
        super.onDestroy()
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        ts_version.apply {
            ts_version.setFactory { TextView(context) }
            setInAnimation(context, R.anim.fade_in)
            setOutAnimation(context, R.anim.fade_out)
            ts_version.setText(BuildConfig.VERSION_NAME)
        }

        imv_app_icon.setOnClickListener { view ->
            appIconClickCount++
            view.startAnimation(anim)
            maybeShowFullBuildVersion()
        }
    }

    private fun maybeShowFullBuildVersion() = dialog?.apply {
        if (BuildConfig.DEBUG && appIconClickCount >= 10) {
            val fullBuildVersion = "${BuildConfig.VERSION_NAME}(${BuildConfig.BUILD_TIME})"
            ts_version.setText(fullBuildVersion)
        }
    }

    companion object {

        // Factory
        fun newInstance() = AppInfoDialog()

    }

}
