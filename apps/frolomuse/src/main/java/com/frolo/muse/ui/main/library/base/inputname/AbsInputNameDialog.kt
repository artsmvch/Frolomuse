package com.frolo.muse.ui.main.library.base.inputname

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.frolo.muse.R
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.views.Anim
import com.frolo.muse.views.getNonNullText
import kotlinx.android.synthetic.main.dialog_abs_input_name.*


abstract class AbsInputNameDialog : BaseDialogFragment() {

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            setContentView(R.layout.dialog_abs_input_name)

            setupDialogSizeByDefault(this)

            loadUI(this)

            window?.apply {
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                edt_name.requestFocus()
                // This simulates a click on the edit text
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        tv_title.text = onGetTitle()

        til_name.hint = onGetHint()

        edt_name.setText(onGetInitialText())

        // Intercept any touches on this overlay
        inc_progress_overlay.setOnClickListener { }

        btn_cancel.setOnClickListener {
            dismiss()
        }

        btn_add.setOnClickListener {
            val name = edt_name.getNonNullText()
            onSaveButtonClick(name)
        }
    }

    protected fun displayError(err: Throwable) {
        Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show()
    }

    protected fun displayInputError(err: Throwable) {
        dialog?.apply {
            til_name.error = err.message
        }
    }

    protected fun setIsLoading(isLoading: Boolean) {
        dialog?.apply {
            if (isLoading) {
                Anim.fadeIn(inc_progress_overlay)
            } else {
                Anim.fadeOut(inc_progress_overlay)
            }
        }
    }

    protected abstract fun onGetTitle(): String

    protected open fun onGetHint(): String? = null

    protected open fun onGetInitialText(): String? = null

    protected abstract fun onSaveButtonClick(name: String)
}
