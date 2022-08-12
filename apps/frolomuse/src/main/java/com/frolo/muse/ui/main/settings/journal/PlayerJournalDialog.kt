package com.frolo.muse.ui.main.settings.journal

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.frolo.muse.R
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_player_journal.*


class PlayerJournalDialog: BaseDialogFragment() {

    private val viewModel by viewModel<PlayerJournalViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeViewModel(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_player_journal)

            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            setupDialogSize(this, 6 * width / 7, ViewGroup.LayoutParams.MATCH_PARENT)

            loadUI(this)
        }
    }

    private fun loadUI(dialog: Dialog) = with(dialog) {
        rv_logs.layoutManager = LinearLayoutManager(context)
        rv_logs.adapter = LogDataItemAdapter()

        btn_copy.setOnClickListener {
            viewModel.onCopyLogsToClipboard()
        }

        btn_send.setOnClickListener {
            viewModel.onSendLogsClicked()
        }

        btn_close.setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        logDataItems.observe(owner) { items ->
            dialog?.apply {
                (rv_logs?.adapter as? LogDataItemAdapter)?.items = items.orEmpty()
            }
        }

        scrollToPosition.observeNonNull(owner) { position ->
            dialog?.apply {
                rv_logs?.scrollToPosition(position)
            }
        }

        notifyLogsCopied.observe(owner) {
            activity?.also { safeActivity ->
                Toast.makeText(safeActivity, R.string.copied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(): PlayerJournalDialog = PlayerJournalDialog()
    }

}