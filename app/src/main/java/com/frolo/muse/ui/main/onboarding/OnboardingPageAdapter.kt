package com.frolo.muse.ui.main.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import kotlinx.android.synthetic.main.item_onboarding_page.view.*


class OnboardingPageAdapter(
    private val items: List<OnboardingPageInfo>
) : RecyclerView.Adapter<OnboardingPageAdapter.Holder>() {

    override fun getItemCount() = items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding_page, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(item: OnboardingPageInfo) = with(itemView) {
            imv_image.setImageResource(item.imageId)
            tv_title.setText(item.titleId)
            tv_description.setText(item.descriptionId)
        }

    }
}