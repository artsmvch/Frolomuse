package com.frolo.muse.ui.main.settings.premium

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.frolo.muse.R
import com.frolo.muse.inflateChild
import kotlinx.android.synthetic.main.item_premium_benefit.view.*


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

        fun bind(benefit: Benefit) = with(itemView) {
            when (benefit.icon) {
                Benefit.Icon.CHECK -> imv_benefit_icon.setImageResource(R.drawable.ic_premium_check)
                Benefit.Icon.LOVE -> imv_benefit_icon.setImageResource(R.drawable.ic_filled_heart)
            }
            tv_benefit_text.text = benefit.text
        }

    }

}