package com.frolo.muse.ui.main.settings.library.filter

import androidx.recyclerview.widget.DiffUtil


class SongFilterItemDiffCallback(
    private val oldList: List<SongFilterItem>,
    private val newList: List<SongFilterItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].type == newList[newItemPosition].type
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return EMPTY_PAYLOAD
    }

    companion object {
        private val EMPTY_PAYLOAD = Any()
    }

}