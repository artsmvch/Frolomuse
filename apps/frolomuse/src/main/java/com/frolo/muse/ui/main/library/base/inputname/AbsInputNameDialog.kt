package com.frolo.muse.ui.main.library.base.inputname

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.frolo.muse.databinding.DialogAbsInputNameBinding
import com.frolo.muse.ui.base.BaseDialogFragment
import com.frolo.muse.views.Anim
import com.frolo.muse.views.getNonNullText


abstract class AbsInputNameDialog : BaseDialogFragment() {
    private var _binding: DialogAbsInputNameBinding? = null
    private val binding: DialogAbsInputNameBinding get() = _binding!!

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            _binding = DialogAbsInputNameBinding.inflate(layoutInflater)

            setContentView(binding.root)

            setupDialogSizeByDefault(this)

            loadUI(this)

            window?.apply {
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                binding.edtName.requestFocus()
                // This simulates a click on the edit text
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        binding.tvTitle.text = onGetTitle()

        binding.tilName.hint = onGetHint()

        binding.edtName.setText(onGetInitialText())

        // Intercept any touches on this overlay
        binding.incProgressOverlay.root.setOnClickListener { }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnAdd.setOnClickListener {
            val name = binding.edtName.getNonNullText()
            onSaveButtonClick(name)
        }
    }

    protected fun displayError(err: Throwable) {
        Toast.makeText(context, err.message, Toast.LENGTH_SHORT).show()
    }

    protected fun displayInputError(err: Throwable) {
        dialog?.apply {
            binding.tilName.error = err.message
        }
    }

    protected fun setIsLoading(isLoading: Boolean) {
        dialog?.apply {
            if (isLoading) {
                Anim.fadeIn(binding.incProgressOverlay.root)
            } else {
                Anim.fadeOut(binding.incProgressOverlay.root)
            }
        }
    }

    protected abstract fun onGetTitle(): String

    protected open fun onGetHint(): String? = null

    protected open fun onGetInitialText(): String? = null

    protected abstract fun onSaveButtonClick(name: String)
}
