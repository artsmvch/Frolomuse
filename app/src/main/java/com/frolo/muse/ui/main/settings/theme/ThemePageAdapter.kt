package com.frolo.muse.ui.main.settings.theme

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlin.properties.Delegates


class ThemePageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    var pages: List<ThemePage> by Delegates.observable(emptyList()) { _, oldList, newList ->
        val diffCallback = ThemePageItemDiffCallback(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = pages.count()

    override fun createFragment(position: Int): Fragment {
        return ThemePageFragment.newInstance(pages[position])
    }

}