package com.frolo.muse.ui.main.settings.premium

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.frolo.core.ui.inflateChild
import com.frolo.muse.R


class BenefitAdapter(private val benefits: List<Benefit>) : RecyclerView.Adapter<BenefitAdapter.ViewHolder>() {

    override fun getItemCount(): Int = benefits.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = parent.inflateChild(R.layout.item_premium_benefit)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(benefits[position])
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val imvBenefitIcon = itemView.findViewById<ImageView>(R.id.imv_benefit_icon)
        private val tvBenefitText = itemView.findViewById<TextView>(R.id.tv_benefit_text)

        fun bind(benefit: Benefit) {
            imvBenefitIcon.setImageResource(benefit.iconId)
            tvBenefitText.text = benefit.text
        }

    }

}