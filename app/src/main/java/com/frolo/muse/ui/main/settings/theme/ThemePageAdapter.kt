package com.frolo.muse.ui.main.settings.theme

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.properties.Delegates


class ThemePageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    var pages: List<ThemePage> by Delegates.observable(emptyList()) { _, oldList, newList ->
        val diffCallback = DiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = pages.count()

    override fun createFragment(position: Int): Fragment {
        return ThemePageFragment.newInstance(pages[position])
    }

    private class DiffCallback(
        val oldList: List<ThemePage>?,
        val newList: List<ThemePage>?
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

}