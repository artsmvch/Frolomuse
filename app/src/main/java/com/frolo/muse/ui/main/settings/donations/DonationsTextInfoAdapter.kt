package com.frolo.muse.ui.main.settings.donations

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.frolo.muse.DebugUtils
import com.frolo.muse.R
import com.frolo.muse.inflateChild


internal class DonationsTextInfoAdapter : RecyclerView.Adapter<DonationsTextInfoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_donations_info_text)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.also { itemView ->
            val layoutParams = itemView.layoutParams
            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                layoutParams.isFullSpan = true
            } else {
                DebugUtils.dumpOnMainThread(IllegalStateException(
                        "The layout manager is expected to be StaggeredLayoutManager"))
            }
        }
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

}