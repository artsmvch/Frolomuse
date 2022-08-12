package com.frolo.muse.ui.main.settings.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import kotlinx.android.synthetic.main.item_player_journal_log_data.view.*
import kotlin.properties.Delegates


class LogDataItemAdapter: RecyclerView.Adapter<LogDataItemAdapter.LogDataItemViewHolder>() {

    var items: List<LogDataItem> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogDataItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_player_journal_log_data, parent, false)
        return LogDataItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LogDataItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class LogDataItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(item: LogDataItem) {
            with(itemView) {
                tv_time.text = item.time
                tv_message.text = item.message
                tv_error_stack_trace.text = item.errorStackTrace
                tv_error_stack_trace.isVisible = !item.errorStackTrace.isNullOrBlank()
            }
        }

    }
}