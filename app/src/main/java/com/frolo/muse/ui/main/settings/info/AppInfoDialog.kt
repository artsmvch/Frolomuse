package com.frolo.muse.ui.main.settings.info


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.frolo.muse.BuildConfig
import com.frolo.muse.R
import kotlinx.android.synthetic.main.dialog_app_info.*

class AppInfoDialog : DialogFragment() {
    companion object {
        fun newInstance() = AppInfoDialog()
    }

    private lateinit var anim: Animation

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
            iniUI(this)
        }
    }

    private fun iniUI(dialog: Dialog) {
        with(dialog) {
            tv_version.text = BuildConfig.VERSION_NAME
            imv_app_icon.setOnClickListener {
                it.startAnimation(anim)
            }
            Glide.with(this@AppInfoDialog)
                    .asBitmap()
                    .load(R.drawable.png_note_512x512)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    //.centerCrop()
                    //.circleCrop()
                    .into(imv_app_icon)
        }
    }
}
