package com.frolo.muse.ui.main.settings.donations

import androidx.recyclerview.widget.DiffUtil


internal class DonationItemDiffCallback constructor(
    private val oldList: List<DonationItem>,
    private val newList: List<DonationItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem is DonationItem.Rating && newItem is DonationItem.Rating) {
            return oldItem == newItem
        }

        if (oldItem is DonationItem.Purchase && newItem is DonationItem.Purchase) {
            return oldItem.productDetails.productId == newItem.productDetails.productId
        }

        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return Any()
    }
}