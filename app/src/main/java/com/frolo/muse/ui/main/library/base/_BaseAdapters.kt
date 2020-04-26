package com.frolo.muse.ui.main.library.base

import com.frolo.muse.util.CharSequences


fun <T> BaseAdapter<T, *>.sectionIndexAt(position: Int, onProvideName: T.() -> String?): CharSequence {
    if (position < 0 || position >= itemCount)
        return CharSequences.empty()

    val item = getItemAt(position)
    val itemName = item.onProvideName()

    if (itemName.isNullOrEmpty())
        return CharSequences.empty()

    return CharSequences.firstCharOrEmpty(itemName)
}