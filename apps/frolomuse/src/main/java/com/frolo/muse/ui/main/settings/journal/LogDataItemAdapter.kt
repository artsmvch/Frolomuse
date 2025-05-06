package com.frolo.muse.ui.main.settings.journal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
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
        private val tvTime = itemView.findViewById<TextView>(R.id.tv_time)
        private val tvMessage = itemView.findViewById<TextView>(R.id.tv_message)
        private val tvErrorStackTrace = itemView.findViewById<TextView>(R.id.tv_error_stack_trace)

        fun bind(item: LogDataItem) {
            tvTime.text = item.time
            tvMessage.text = item.message
            tvErrorStackTrace.text = item.errorStackTrace
            tvErrorStackTrace.isVisible = !item.errorStackTrace.isNullOrBlank()
        }

    }
}