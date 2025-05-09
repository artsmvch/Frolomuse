package com.frolo.muse.ui.main.settings.hidden

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.databinding.DialogHiddenFilesBinding
import com.frolo.muse.ui.base.BaseDialogFragment


class HiddenFilesDialog : BaseDialogFragment() {
    private var _binding: DialogHiddenFilesBinding? = null
    private val binding: DialogHiddenFilesBinding get() = _binding!!

    private val viewModel: HiddenFilesViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            _binding = DialogHiddenFilesBinding.inflate(layoutInflater)
            setContentView(binding.root)
            loadUi(this)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            setupDialogSize(this, 10 * width / 11, 10 * height / 11)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUi(dialog: Dialog) = with(binding) {
        btnOk.setOnClickListener {
            dismiss()
        }

        rvFiles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = HiddenFileAdapter {
                viewModel.onRemoveClick(it)
            }
            ContextCompat.getDrawable(context, R.drawable.list_thin_divider_tinted)?.also { d ->
                val decor = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                decor.setDrawable(d)
                addItemDecoration(decor)
            }
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        hiddenFiles.observeNonNull(owner) {
            dialog?.apply {
                (binding.rvFiles.adapter as? HiddenFileAdapter)?.submitList(it)
            }
        }

        placeholderVisible.observeNonNull(owner) {
            dialog?.apply {
                binding.viewPlaceholder.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        isLoading.observeNonNull(owner) {
            dialog?.apply {
                binding.pbLoading.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        error.observeNonNull(owner) {
            postError(it)
        }
    }

    companion object {
        fun newInstance() = HiddenFilesDialog()
    }

}