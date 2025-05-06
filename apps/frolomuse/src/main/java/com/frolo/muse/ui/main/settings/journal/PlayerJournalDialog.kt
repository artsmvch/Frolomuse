package com.frolo.muse.ui.main.settings.journal

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.arch.support.observe
import com.frolo.arch.support.observeNonNull
import com.frolo.muse.ui.base.BaseDialogFragment


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
        findViewById<RecyclerView>(R.id.rv_logs).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = LogDataItemAdapter()
        }

        findViewById<View>(R.id.btn_copy).setOnClickListener {
            viewModel.onCopyLogsToClipboard()
        }

        findViewById<View>(R.id.btn_send).setOnClickListener {
            viewModel.onSendLogsClicked()
        }

        findViewById<View>(R.id.btn_close).setOnClickListener {
            dismiss()
        }
    }

    private fun observeViewModel(owner: LifecycleOwner) = with(viewModel) {
        logDataItems.observe(owner) { items ->
            dialog?.apply {
                (findViewById<RecyclerView>(R.id.rv_logs)?.adapter as? LogDataItemAdapter)?.items = items.orEmpty()
            }
        }

        scrollToPosition.observeNonNull(owner) { position ->
            dialog?.apply {
                findViewById<RecyclerView>(R.id.rv_logs)?.scrollToPosition(position)
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