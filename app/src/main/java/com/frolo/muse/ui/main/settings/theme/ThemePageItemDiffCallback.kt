package com.frolo.muse.ui.main.settings.theme

import androidx.recyclerview.widget.DiffUtil


class ThemePageItemDiffCallback(
    private val oldList: List<ThemePage>?,
    private val newList: List<ThemePage>?
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList?.size ?: 0

    override fun getNewListSize(): Int = newList?.size ?: 0

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList!![oldItemPosition].theme == newList!![newItemPosition].theme
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList!![oldItemPosition] == newList!![newItemPosition]
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Do not trigger the change animation
        return Any()
    }

}