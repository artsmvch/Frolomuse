package com.frolo.muse.ui.main.settings.theme

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import kotlin.properties.Delegates


class ThemePageFragmentAdapter(private val fragment: Fragment) : FragmentStateAdapter(fragment), AbsThemePageAdapter {

    override var pages: List<ThemePage> by Delegates.observable(emptyList()) { _, oldList, newList ->
        val diffCallback = ThemePageItemDiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = pages.count()

    override fun getItemId(position: Int): Long = pages[position].uniqueId

    override fun containsItem(itemId: Long): Boolean {
        return pages.indexOfFirst { page -> page.uniqueId == itemId } >= 0
    }

    override fun createFragment(position: Int): Fragment {
        return ThemePageFragment.newInstance(pages[position])
    }

    /**
     * A necessary workaround to update items by a position.
     */
    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            // I hate android devs, why should I do this and why can't it work properly right away?
            val tag: String = "f" + holder.itemId
            val childFragment: Fragment? = fragment.childFragmentManager.findFragmentByTag(tag)
            if (childFragment is ThemePageFragment) {
                childFragment.updateArgument(pages[position])
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

}