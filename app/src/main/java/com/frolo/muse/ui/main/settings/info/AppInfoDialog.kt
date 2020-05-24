package com.frolo.muse.ui.main.settings.info

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
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
     * Indicates whether the app icon has been clicked at least once.
     */
    private var appIconClicked: Boolean = false

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
        eventLogger.logEasterEggFound(clicked = appIconClicked)
        super.onDestroy()
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        tv_version.text = BuildConfig.VERSION_NAME

        imv_app_icon.setOnClickListener {
            appIconClicked = true
            it.startAnimation(anim)
        }
    }

    companion object {

        // Factory
        fun newInstance() = AppInfoDialog()

    }

}
