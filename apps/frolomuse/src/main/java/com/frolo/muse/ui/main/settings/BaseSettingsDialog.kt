package com.frolo.muse.ui.main.settings

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_base_settings.*


abstract class BaseSettingsDialog : BaseDialogFragment() {

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            setContentView(R.layout.dialog_base_settings)

            setupDialogSizeByDefault(this)

            onSetupTitle(tv_title)
            onSetupContentView(fl_content)
            onSetupAction(btn_action)
        }
    }

    protected abstract fun onSetupTitle(titleView: TextView)

    protected abstract fun onSetupContentView(contentView: ViewGroup)

    protected abstract fun onSetupAction(actionView: TextView)

}