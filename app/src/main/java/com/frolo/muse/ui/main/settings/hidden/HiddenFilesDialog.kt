package com.frolo.muse.ui.main.settings.hidden

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.muse.arch.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_hidden_files.*


class HiddenFilesDialog : BaseDialogFragment() {

    companion object {
        fun newInstance() = HiddenFilesDialog()
    }

    private val viewModel: HiddenFilesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(R.layout.dialog_hidden_files)
            initUI(this)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            setupDialogSize(this, 10 * width / 11, 10 * height / 11)
        }
    }

    private fun initUI(dialog: Dialog) {
        with(dialog) {
            btn_ok.setOnClickListener {
                dismiss()
            }

            rv_files.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = HiddenFileAdapter {
                    viewModel.onRemoveClick(it)
                }
            }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) {
        viewModel.apply {
            hiddenFiles.observeNonNull(owner) {
                dialog?.apply {
                    (rv_files.adapter as? HiddenFileAdapter)?.submitList(it)
                }
            }

            placeholderVisible.observeNonNull(owner) {
                dialog?.apply {
                    view_placeholder.visibility = if (it) View.VISIBLE else View.GONE
                }
            }

            isLoading.observeNonNull(owner) {
                dialog?.apply {
                    pb_loading.visibility = if (it) View.VISIBLE else View.GONE
                }
            }

            error.observeNonNull(owner) {
                postError(it)
            }
        }
    }

}