package com.frolo.muse.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.onboarding.databinding.ItemOnboardingPageBinding


internal class OnboardingPageAdapter(
    private val items: List<OnboardingPageInfo>
) : RecyclerView.Adapter<OnboardingPageAdapter.Holder>() {

    override fun getItemCount() = items.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_onboarding_page, parent, false)
        return Holder(itemView)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val binding = ItemOnboardingPageBinding.bind(itemView)

        fun bind(item: OnboardingPageInfo) = with(itemView) {
            binding.imvImage.setImageResource(item.imageId)
            binding.tvTitle.setText(item.titleId)
            binding.tvDescription.setText(item.descriptionId)
        }
    }
}