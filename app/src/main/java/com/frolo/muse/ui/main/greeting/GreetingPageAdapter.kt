package com.frolo.muse.ui.main.greeting

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import kotlinx.android.synthetic.main.item_greeting_page.view.*


class GreetingPageAdapter(
    private val items: List<GreetingPageInfo>
) : RecyclerView.Adapter<GreetingPageAdapter.Holder>() {

    override fun getItemCount() = items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_greeting_page, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(item: GreetingPageInfo) = with(itemView) {
            imv_image.setImageResource(item.imageId)
            tv_title.setText(item.titleId)
            tv_description.setText(item.descriptionId)
        }

    }
}